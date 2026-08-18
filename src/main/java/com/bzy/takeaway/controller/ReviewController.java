package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Review;
import com.bzy.takeaway.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping("/store/{storeId}")
    public Result<Map<String, Object>> getStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer ratingFilter) {
        var p = reviewService.getStoreReviews(storeId, page, size, ratingFilter);
        return Result.ok(Map.of(
            "list", p.getRecords(),
            "total", p.getTotal(),
            "page", p.getCurrent(),
            "size", p.getSize()
        ));
    }
    
    @PostMapping
    public Result<Review> submitReview(
            @RequestAttribute(required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        // 未登录时返回错误
        if (userId == null) {
            return Result.fail("请先登录");
        }
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");
        
        Review review = reviewService.submitReview(userId, orderId, rating, content);
        return Result.ok(review);
    }
    
    @GetMapping("/my")
    public Result<List<Review>> getMyReviews(
            @RequestAttribute(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 未登录时返回空列表
        if (userId == null) {
            return Result.ok(List.of());
        }
        return Result.ok(reviewService.getMyReviews(userId, page, size));
    }
}
