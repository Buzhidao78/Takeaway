package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.OrderItem;
import com.bzy.takeaway.entity.Orders;
import com.bzy.takeaway.entity.Review;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.mapper.OrderItemMapper;
import com.bzy.takeaway.mapper.OrdersMapper;
import com.bzy.takeaway.mapper.ReviewMapper;
import com.bzy.takeaway.mapper.StoreMapper;
import com.bzy.takeaway.service.NotificationService;
import com.bzy.takeaway.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final StoreMapper storeMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final NotificationService notificationService;

    @Override
    public Page<Review> getStoreReviews(Long storeId, Integer page, Integer size, Integer ratingFilter) {
        LambdaQueryWrapper<Review> q = new LambdaQueryWrapper<Review>()
            .eq(Review::getStoreId, storeId)
            .eq(Review::getStatus, 1)
            .eq(Review::getDeleted, 0)
            .orderByDesc(Review::getCreateTime);

        if (ratingFilter != null && ratingFilter > 0) {
            q.eq(Review::getRating, ratingFilter);
        }

        return reviewMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    public Review submitReview(Long userId, Long orderId, Integer rating, String content) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权评价该订单");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("订单未完成，不能评价");
        }

        LambdaQueryWrapper<Review> existing = new LambdaQueryWrapper<Review>()
            .eq(Review::getOrderId, orderId)
            .eq(Review::getDeleted, 0);
        if (reviewMapper.selectCount(existing) > 0) {
            throw new RuntimeException("该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setStoreId(order.getStoreId());
        review.setRating(rating);
        review.setContent(content);
        review.setStatus(1);
        reviewMapper.insert(review);

        updateStoreRating(order.getStoreId());

        // 发送评价通知给商家（失败不影响主流程）
        try {
            log.info("开始发送评价通知 - 订单ID: {}, 商店ID: {}", orderId, order.getStoreId());
            Store store = storeMapper.selectById(order.getStoreId());
            log.info("查询商店结果 - 商店ID: {}, 商店对象: {}, 商家用户ID: {}",
                order.getStoreId(), store, store != null ? store.getUserId() : "null");
            if (store != null && store.getUserId() != null) {
                String ratingText = rating >= 4 ? "好评" : (rating <= 2 ? "差评" : "中评");
                String notificationContent = String.format("用户%s给了您%s（%d星）：%s",
                    "用户", ratingText, rating, content != null && content.length() > 20 ? content.substring(0, 20) + "..." : content);
                log.info("准备发送通知 - 商家用户ID: {}, 标题: {}, 内容: {}, 类型: review, 关联ID: {}",
                    store.getUserId(), "收到新评价", notificationContent, store.getId());
                notificationService.sendNotification(store.getUserId(), "收到新评价",
                    notificationContent,
                    "review", store.getId());
                log.info("评价通知发送成功");
            } else {
                log.warn("无法发送评价通知 - 商店不存在或商家用户ID为空");
            }
        } catch (Exception e) {
            log.error("发送评价通知失败", e);
        }

        return review;
    }

    @Override
    @Transactional
    public void updateStoreRating(Long storeId) {
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getStoreId, storeId)
                .eq(Review::getStatus, 1)
                .eq(Review::getDeleted, 0)
        );

        if (reviews.isEmpty()) {
            Store store = storeMapper.selectById(storeId);
            if (store != null) {
                store.setRating(BigDecimal.ZERO);
                store.setReviewCount(0);
                storeMapper.updateById(store);
            }
            return;
        }

        BigDecimal totalRating = reviews.stream()
            .map(Review::getRating)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRating = totalRating
            .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);

        Store store = storeMapper.selectById(storeId);
        if (store != null) {
            store.setRating(avgRating);
            store.setReviewCount(reviews.size());
            storeMapper.updateById(store);
        }
    }

    @Override
    public Page<Review> getMerchantReviews(Long storeId, Integer page, Integer size) {
        LambdaQueryWrapper<Review> q = new LambdaQueryWrapper<Review>()
            .eq(Review::getStoreId, storeId)
            .eq(Review::getDeleted, 0)
            .orderByDesc(Review::getCreateTime);

        return reviewMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    @Transactional
    public Review replyReview(Long userId, Long reviewId, String replyContent) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }

        Store store = storeMapper.selectOne(
            new LambdaQueryWrapper<Store>()
                .eq(Store::getId, review.getStoreId())
                .eq(Store::getUserId, userId)
        );
        if (store == null) {
            throw new RuntimeException("无权回复该评论");
        }

        review.setReplyContent(replyContent);
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);

        return review;
    }

    @Override
    @Transactional
    public Review updateReviewStatus(Long userId, Long reviewId, Integer status) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }

        Store store = storeMapper.selectOne(
            new LambdaQueryWrapper<Store>()
                .eq(Store::getId, review.getStoreId())
                .eq(Store::getUserId, userId)
        );
        if (store == null) {
            throw new RuntimeException("无权操作该评论");
        }

        review.setStatus(status);
        reviewMapper.updateById(review);

        updateStoreRating(review.getStoreId());

        return review;
    }

    @Override
    public List<Review> getMyReviews(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Review> q = new LambdaQueryWrapper<Review>()
            .eq(Review::getUserId, userId)
            .eq(Review::getDeleted, 0)
            .orderByDesc(Review::getCreateTime);

        return reviewMapper.selectPage(new Page<>(page, size), q).getRecords();
    }
}