package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bzy.takeaway.entity.Category;
import com.bzy.takeaway.entity.Dish;
import com.bzy.takeaway.entity.ProductReview;
import com.bzy.takeaway.entity.ReviewVote;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.mapper.CategoryMapper;
import com.bzy.takeaway.mapper.DishMapper;
import com.bzy.takeaway.mapper.ProductReviewMapper;
import com.bzy.takeaway.mapper.ReviewVoteMapper;
import com.bzy.takeaway.mapper.StoreMapper;
import com.bzy.takeaway.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Slf4j
@Service
public class ProductReviewService extends ServiceImpl<ProductReviewMapper, ProductReview> {
    
    @Autowired
    private ProductReviewMapper productReviewMapper;
    
    @Autowired
    private ReviewVoteMapper reviewVoteMapper;
    
    @Autowired
    private StoreMapper storeMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    public StoreMapper getStoreMapper() {
        return storeMapper;
    }
    
    @Autowired
    private NotificationService notificationService;
    
    public Page<ProductReview> getDishReviews(Long dishId, int page, int size, Boolean withImage, Integer rating) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getDishId, dishId)
            .eq(ProductReview::getStatus, 1);
        
        if (withImage != null && withImage) {
            q.isNotNull(ProductReview::getImages)
             .ne(ProductReview::getImages, "");
        }
        
        if (rating != null) {
            q.eq(ProductReview::getRating, rating);
        }
        
        q.orderByDesc(ProductReview::getCreateTime);
        Page<ProductReview> pageResult = productReviewMapper.selectPage(new Page<>(page, size), q);
        
        // 为每个评价设置用户名
        for (ProductReview review : pageResult.getRecords()) {
            if (review.getUserId() != null) {
                SysUser user = baseMapper.getSysUserById(review.getUserId());
                if (user != null) {
                    review.setUserName(user.getNickname());
                }
            }
        }
        
        return pageResult;
    }
    
    public Page<ProductReview> getMyReviews(Long userId, int page, int size) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getUserId, userId)
            .orderByDesc(ProductReview::getCreateTime);
        return productReviewMapper.selectPage(new Page<>(page, size), q);
    }
    
    public List<ProductReview> getOrderByUserReviews(Long orderId, Long userId) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getOrderId, orderId)
            .eq(ProductReview::getUserId, userId);
        return productReviewMapper.selectList(q);
    }
    
    @Transactional
    public void submitOrUpdateReview(ProductReview review, Long userId) {
        // 查询是否已经评价过
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getOrderItemId, review.getOrderItemId())
            .eq(ProductReview::getUserId, userId);
        ProductReview existingReview = productReviewMapper.selectOne(q);
        
        if (existingReview != null) {
            // 已评价，追加评价（追评）
            existingReview.setAdditionalContent(review.getContent());
            existingReview.setAdditionalImages(review.getImages());
            existingReview.setAdditionalTime(java.time.LocalDateTime.now());
            // 如果追评有评分，更新总评分
            if (review.getRating() != null) {
                existingReview.setRating(review.getRating());
            }
            productReviewMapper.updateById(existingReview);
            System.out.println("追评成功，ID=" + existingReview.getId());
            
            // 更新店铺评分
            if (review.getStoreId() != null) {
                updateStoreRating(review.getStoreId());
            }
        } else {
            // 首次评价
            productReviewMapper.insert(review);
            System.out.println("评价成功，ID=" + review.getId());
            
            // 更新店铺评分
            if (review.getStoreId() != null) {
                updateStoreRating(review.getStoreId());
            }
            
            // 发送评价通知给商家（失败不影响主流程）
            try {
                log.info("开始发送商品评价通知 - 订单ID: {}, 商店ID: {}, 评分: {}", 
                    review.getOrderId(), review.getStoreId(), review.getRating());
                
                if (review.getStoreId() != null) {
                    Store store = storeMapper.selectById(review.getStoreId());
                    log.info("查询商店结果 - 商店ID: {}, 商店对象: {}, 商家用户ID: {}", 
                        review.getStoreId(), store, store != null ? store.getUserId() : "null");
                    
                    if (store != null && store.getUserId() != null) {
                        String ratingText = review.getRating() >= 4 ? "好评" : (review.getRating() <= 2 ? "差评" : "中评");
                        String notificationContent = String.format("用户给了您%s（%d星）：%s", 
                            ratingText, review.getRating(), 
                            review.getContent() != null && review.getContent().length() > 20 
                                ? review.getContent().substring(0, 20) + "..." 
                                : review.getContent());
                        
                        log.info("准备发送通知 - 商家用户ID: {}, 标题: {}, 内容: {}, 类型: review, 关联ID: {}", 
                            store.getUserId(), "收到新评价", notificationContent, store.getId());
                        
                        notificationService.sendNotification(store.getUserId(), "收到新评价", 
                            notificationContent, "review", store.getId());
                        
                        log.info("商品评价通知发送成功");
                    } else {
                        log.warn("无法发送商品评价通知 - 商店不存在或商家用户ID为空");
                    }
                }
            } catch (Exception e) {
                log.error("发送商品评价通知失败", e);
            }
        }
    }
    
    @Transactional
    public void replyReview(Long reviewId, String content) {
        var review = productReviewMapper.selectById(reviewId);
        if (review != null) {
            review.setReplyContent(content);
            review.setReplyTime(LocalDateTime.now());
            productReviewMapper.updateById(review);
        }
    }
    
    public void updateStatus(Long reviewId, int status) {
        var review = productReviewMapper.selectById(reviewId);
        if (review != null) {
            review.setStatus(status);
            productReviewMapper.updateById(review);
        }
    }
    
    @Transactional
    public Map<String, Object> likeReview(Long reviewId, Long userId) {
        var review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 检查用户是否已经投过票
        ReviewVote existingVote = reviewVoteMapper.selectByReviewIdAndUserId(reviewId, userId);
        
        Map<String, Object> result = new HashMap<>();
        
        if (existingVote == null) {
            // 第一次投票，点赞
            ReviewVote vote = new ReviewVote();
            vote.setReviewId(reviewId);
            vote.setUserId(userId);
            vote.setVoteType(1);  // 1=点赞
            reviewVoteMapper.insert(vote);
            
            // 更新点赞数
            Integer currentLikes = review.getLikeCount() != null ? review.getLikeCount() : 0;
            review.setLikeCount(currentLikes + 1);
            productReviewMapper.updateById(review);
            
            result.put("action", "liked");
            result.put("likeCount", review.getLikeCount());
            result.put("dislikeCount", review.getDislikeCount());
            result.put("userVote", 1);
        } else if (existingVote.getVoteType() == 1) {
            // 已经点过赞，取消点赞
            reviewVoteMapper.deleteById(existingVote.getId());
            
            Integer currentLikes = review.getLikeCount() != null ? review.getLikeCount() : 0;
            review.setLikeCount(Math.max(0, currentLikes - 1));
            productReviewMapper.updateById(review);
            
            result.put("action", "cancelled");
            result.put("likeCount", review.getLikeCount());
            result.put("dislikeCount", review.getDislikeCount());
            result.put("userVote", 0);
        } else {
            // 之前点过踩，现在改为点赞
            existingVote.setVoteType(1);
            reviewVoteMapper.updateById(existingVote);
            
            // 更新点赞数和点踩数
            Integer currentLikes = review.getLikeCount() != null ? review.getLikeCount() : 0;
            Integer currentDislikes = review.getDislikeCount() != null ? review.getDislikeCount() : 0;
            review.setLikeCount(currentLikes + 1);
            review.setDislikeCount(Math.max(0, currentDislikes - 1));
            productReviewMapper.updateById(review);
            
            result.put("action", "changed");
            result.put("likeCount", review.getLikeCount());
            result.put("dislikeCount", review.getDislikeCount());
            result.put("userVote", 1);
        }
        
        return result;
    }
    
    @Transactional
    public Map<String, Object> dislikeReview(Long reviewId, Long userId) {
        var review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 检查用户是否已经投过票
        ReviewVote existingVote = reviewVoteMapper.selectByReviewIdAndUserId(reviewId, userId);
        
        Map<String, Object> result = new HashMap<>();
        
        if (existingVote == null) {
            // 第一次投票，点踩
            ReviewVote vote = new ReviewVote();
            vote.setReviewId(reviewId);
            vote.setUserId(userId);
            vote.setVoteType(-1);  // -1=点踩
            reviewVoteMapper.insert(vote);
            
            // 更新点踩数
            Integer currentDislikes = review.getDislikeCount() != null ? review.getDislikeCount() : 0;
            review.setDislikeCount(currentDislikes + 1);
            productReviewMapper.updateById(review);
            
            result.put("action", "disliked");
            result.put("likeCount", review.getLikeCount());
            result.put("dislikeCount", review.getDislikeCount());
            result.put("userVote", -1);
        } else if (existingVote.getVoteType() == -1) {
            // 已经点过踩，取消点踩
            reviewVoteMapper.deleteById(existingVote.getId());
            
            Integer currentDislikes = review.getDislikeCount() != null ? review.getDislikeCount() : 0;
            review.setDislikeCount(Math.max(0, currentDislikes - 1));
            productReviewMapper.updateById(review);
            
            result.put("action", "cancelled");
            result.put("likeCount", review.getLikeCount());
            result.put("dislikeCount", review.getDislikeCount());
            result.put("userVote", 0);
        } else {
            // 之前点过赞，现在改为点踩
            existingVote.setVoteType(-1);
            reviewVoteMapper.updateById(existingVote);
            
            // 更新点赞数和点踩数
            Integer currentLikes = review.getLikeCount() != null ? review.getLikeCount() : 0;
            Integer currentDislikes = review.getDislikeCount() != null ? review.getDislikeCount() : 0;
            review.setLikeCount(Math.max(0, currentLikes - 1));
            review.setDislikeCount(currentDislikes + 1);
            productReviewMapper.updateById(review);
            
            result.put("action", "changed");
            result.put("likeCount", review.getLikeCount());
            result.put("dislikeCount", review.getDislikeCount());
            result.put("userVote", -1);
        }
        
        return result;
    }
    
    public Integer getUserVote(Long reviewId, Long userId) {
        ReviewVote vote = reviewVoteMapper.selectByReviewIdAndUserId(reviewId, userId);
        return vote != null ? vote.getVoteType() : 0;
    }
    
    public Page<ProductReview> getStoreReviews(Long storeId, int page, int size, Integer status, 
                                               Integer rating, Boolean withImage, String sortBy) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getStoreId, storeId);
        
        // 默认只查询已审核通过的评价（status=1）
        if (status == null) {
            q.eq(ProductReview::getStatus, 1);
        } else {
            q.eq(ProductReview::getStatus, status);
        }
        
        // 评分筛选（支持范围筛选）
        if (rating != null) {
            if (rating == 5) {
                // 好评：4-5星
                q.ge(ProductReview::getRating, 4);
            } else if (rating == 3) {
                // 中评：3星
                q.eq(ProductReview::getRating, 3);
            } else if (rating == 1) {
                // 差评：1-2星
                q.le(ProductReview::getRating, 2);
            } else {
                // 其他情况精确匹配
                q.eq(ProductReview::getRating, rating);
            }
        }
        
        // 有图筛选
        if (withImage != null && withImage) {
            q.and(wrapper -> wrapper
                .isNotNull(ProductReview::getImages)
                .ne(ProductReview::getImages, "")
            );
        }
        
        // 排序
        if ("like".equals(sortBy)) {
            // 按热度排序（点赞数）
            q.orderByDesc(ProductReview::getLikeCount)
             .orderByDesc(ProductReview::getCreateTime);
        } else {
            // 默认按时间排序
            q.orderByDesc(ProductReview::getCreateTime);
        }
        
        Page<ProductReview> pageResult = productReviewMapper.selectPage(new Page<>(page, size), q);
        
        // 为每个评价设置用户名
        for (ProductReview review : pageResult.getRecords()) {
            if (review.getUserId() != null) {
                SysUser user = baseMapper.getSysUserById(review.getUserId());
                if (user != null) {
                    review.setUserName(user.getNickname());
                }
            }
        }
        
        return pageResult;
    }
    
    public Page<ProductReview> getMerchantReviews(Long userId, int page, int size, Long categoryId, Integer rating, String keyword) {
        // 通过 userId 获取 store_id
        Store store = storeMapper.selectOne(new LambdaQueryWrapper<Store>().eq(Store::getUserId, userId));
        if (store == null) {
            return new Page<>(page, size);
        }
        Long storeId = store.getId();
        
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getStoreId, storeId);
        
        // 按菜品分类筛选：先找到该分类下的所有菜品 ID
        if (categoryId != null) {
            List<Dish> dishes = dishMapper.selectList(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getStoreId, storeId)
                    .eq(Dish::getCategoryId, categoryId)
            );
            if (dishes.isEmpty()) {
                return new Page<>(page, size);
            }
            List<Long> dishIds = dishes.stream().map(Dish::getId).collect(java.util.stream.Collectors.toList());
            q.in(ProductReview::getDishId, dishIds);
        }
        
        if (rating != null) {
            q.eq(ProductReview::getRating, rating);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 先搜索用户名，获取匹配的用户 ID 列表
            List<SysUser> matchedUsers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                    .like(SysUser::getNickname, keyword)
            );
            List<Long> matchedUserIds = matchedUsers.stream()
                .map(SysUser::getId)
                .collect(java.util.stream.Collectors.toList());
            
            // 搜索条件：内容包含关键词 OR 用户 ID 在匹配列表中
            q.and(w -> {
                w.like(ProductReview::getContent, keyword);
                if (!matchedUserIds.isEmpty()) {
                    w.or().in(ProductReview::getUserId, matchedUserIds);
                }
            });
        }
        
        q.orderByDesc(ProductReview::getCreateTime);
        Page<ProductReview> pageResult = productReviewMapper.selectPage(new Page<>(page, size), q);
        
        // 为每个评价设置用户名和菜品信息
        for (ProductReview review : pageResult.getRecords()) {
            if (review.getUserId() != null) {
                SysUser user = baseMapper.getSysUserById(review.getUserId());
                if (user != null) {
                    review.setUserName(user.getNickname());
                }
            }
            // 设置菜品名称和分类名称
            if (review.getDishId() != null) {
                Dish dish = dishMapper.selectById(review.getDishId());
                if (dish != null) {
                    review.setDishName(dish.getName());
                    if (dish.getCategoryId() != null) {
                        var cat = categoryMapper.selectById(dish.getCategoryId());
                        if (cat != null) {
                            review.setCategoryName(cat.getName());
                        }
                    }
                }
            }
        }
        
        return pageResult;
    }
    
    public Map<String, Object> getReviewStats(Long userId) {
        // 通过 userId 获取 store_id
        Store store = storeMapper.selectOne(new LambdaQueryWrapper<Store>().eq(Store::getUserId, userId));
        if (store == null) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalCount", 0);
            emptyStats.put("averageRating", 0.0);
            emptyStats.put("positiveRate", 0.0);
            emptyStats.put("withImageCount", 0);
            return emptyStats;
        }
        Long storeId = store.getId();
        
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getStoreId, storeId);
        List<ProductReview> reviews = productReviewMapper.selectList(q);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", reviews.size());
        
        if (reviews.isEmpty()) {
            stats.put("averageRating", 0.0);
            stats.put("positiveRate", 0.0);
            stats.put("withImageCount", 0);
            return stats;
        }
        
        double avgRating = reviews.stream()
            .mapToInt(ProductReview::getRating)
            .average()
            .orElse(0.0);
        stats.put("averageRating", Math.round(avgRating * 10) / 10.0);
        
        long positiveCount = reviews.stream()
            .filter(r -> r.getRating() >= 4)
            .count();
        double positiveRate = (double) positiveCount / reviews.size() * 100;
        stats.put("positiveRate", Math.round(positiveRate * 10) / 10.0);
        
        long withImageCount = reviews.stream()
            .filter(r -> r.getImages() != null && !r.getImages().trim().isEmpty())
            .count();
        stats.put("withImageCount", withImageCount);
        
        return stats;
    }
    
    public Double calculateDishRating(Long dishId) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getDishId, dishId)
            .eq(ProductReview::getStatus, 1)
            .select(ProductReview::getRating);
        List<ProductReview> reviews = productReviewMapper.selectList(q);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
            .mapToInt(ProductReview::getRating)
            .average()
            .orElse(0.0);
    }
    
    /**
     * 更新店铺综合评分
     */
    @Transactional
    public void updateStoreRating(Long storeId) {
        List<ProductReview> reviews = productReviewMapper.selectList(
            new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getStoreId, storeId)
                .eq(ProductReview::getStatus, 1)
        );
        
        Store store = storeMapper.selectById(storeId);
        if (store == null) return;
        
        if (reviews.isEmpty()) {
            store.setRating(BigDecimal.ZERO);
            store.setReviewCount(0);
            storeMapper.updateById(store);
            return;
        }
        
        BigDecimal totalRating = reviews.stream()
            .map(ProductReview::getRating)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgRating = totalRating
            .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
        
        store.setRating(avgRating);
        store.setReviewCount(reviews.size());
        storeMapper.updateById(store);
    }
}
