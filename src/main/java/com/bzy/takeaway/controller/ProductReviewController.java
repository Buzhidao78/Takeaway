package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.ProductReview;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product-review")
public class ProductReviewController {
    
    @Autowired
    private ProductReviewService productReviewService;
    
    @GetMapping("/dish/{dishId}")
    public Result<Map<String, Object>> getDishReviews(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean withImage,
            @RequestParam(required = false) Integer rating) {
        var result = new HashMap<String, Object>();
        var pageData = productReviewService.getDishReviews(dishId, page, size, withImage, rating);
        result.put("data", pageData.getRecords());
        result.put("total", pageData.getTotal());
        return Result.ok(result);
    }
    
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyReviews(
            @RequestAttribute(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = new HashMap<String, Object>();
        // 未登录时返回空列表
        if (userId == null) {
            result.put("data", new Object[0]);
            result.put("total", 0);
            return Result.ok(result);
        }
        var pageData = productReviewService.getMyReviews(userId, page, size);
        result.put("data", pageData.getRecords());
        result.put("total", pageData.getTotal());
        return Result.ok(result);
    }
    
    @GetMapping("/order/{orderId}")
    public Result<List<ProductReview>> getOrderReviews(
            @PathVariable Long orderId,
            @RequestAttribute(required = false) Long userId) {
        List<ProductReview> reviews = productReviewService.getOrderByUserReviews(orderId, userId);
        return Result.ok(reviews);
    }
    
    @PostMapping
    public Result<Void> submitReview(
            @RequestAttribute Long userId,
            @RequestBody ProductReview review) {
        try {
            System.out.println("收到评价数据：userId=" + userId + ", images=" + review.getImages());
            // 设置用户 ID
            review.setUserId(userId);
            productReviewService.submitOrUpdateReview(review, userId);
            System.out.println("评价保存成功，ID=" + review.getId());
            return Result.ok(null);
        } catch (Exception e) {
            System.out.println("评价失败：" + e.getMessage());
            return Result.fail("评价失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/reply")
    public Result<Void> replyReview(@RequestBody Map<String, Object> params) {
        try {
            Long reviewId = Long.valueOf(params.get("reviewId").toString());
            String content = params.get("content").toString();
            productReviewService.replyReview(reviewId, content);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail("回复失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Long reviewId = Long.valueOf(params.get("reviewId").toString());
            Integer status = (Integer) params.get("status");
            productReviewService.updateStatus(reviewId, status);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail("操作失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/store/{storeId}")
    public Result<Map<String, Object>> getStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean withImage,
            @RequestParam(defaultValue = "time") String sortBy) {
        var result = new HashMap<String, Object>();
        var pageData = productReviewService.getStoreReviews(storeId, page, size, status, rating, withImage, sortBy);
        result.put("data", pageData.getRecords());
        result.put("total", pageData.getTotal());
        return Result.ok(result);
    }
    
    @GetMapping("/merchant")
    public Result<Map<String, Object>> getMerchantReviews(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean needStats) {
        var result = new HashMap<String, Object>();
        var pageData = productReviewService.getMerchantReviews(userId, page, size, categoryId, rating, keyword);
        result.put("list", pageData.getRecords());
        result.put("total", pageData.getTotal());
        
        if (needStats) {
            var stats = productReviewService.getReviewStats(userId);
            result.put("stats", stats);
        }
        
        return Result.ok(result);
    }
    
    @PostMapping("/merchant/reply")
    public Result<Void> merchantReplyReview(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> params) {
        try {
            Long reviewId = Long.valueOf(params.get("reviewId").toString());
            String content = params.get("content").toString();
            
            var review = productReviewService.getById(reviewId);
            if (review == null) {
                return Result.fail("评价不存在");
            }
            
            // 通过 storeId 获取店铺，再检查店铺是否属于当前用户
            var store = productReviewService.getStoreMapper().selectById(review.getStoreId());
            if (store == null || !store.getUserId().equals(userId)) {
                return Result.fail("无权操作");
            }
            
            productReviewService.replyReview(reviewId, content);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail("回复失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/{reviewId}/like")
    public Result<Map<String, Object>> likeReview(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId) {
        try {
            Map<String, Object> result = productReviewService.likeReview(reviewId, userId);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("操作失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/{reviewId}/dislike")
    public Result<Map<String, Object>> dislikeReview(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId) {
        try {
            Map<String, Object> result = productReviewService.dislikeReview(reviewId, userId);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("操作失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/{reviewId}/vote")
    public Result<Integer> getUserVote(
            @PathVariable Long reviewId,
            @RequestAttribute(required = false) Long userId) {
        if (userId == null) {
            return Result.ok(0);
        }
        Integer vote = productReviewService.getUserVote(reviewId, userId);
        return Result.ok(vote);
    }
}
