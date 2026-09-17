package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.Review;

import java.util.List;

public interface ReviewService {

    Page<Review> getStoreReviews(Long storeId, Integer page, Integer size, Integer ratingFilter);

    Review submitReview(Long userId, Long orderId, Integer rating, String content);

    void updateStoreRating(Long storeId);

    Page<Review> getMerchantReviews(Long storeId, Integer page, Integer size);

    Review replyReview(Long userId, Long reviewId, String replyContent);

    Review updateReviewStatus(Long userId, Long reviewId, Integer status);

    List<Review> getMyReviews(Long userId, Integer page, Integer size);
}