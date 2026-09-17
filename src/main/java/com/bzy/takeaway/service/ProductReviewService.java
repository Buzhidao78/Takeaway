package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bzy.takeaway.entity.ProductReview;
import com.bzy.takeaway.mapper.StoreMapper;

import java.util.List;
import java.util.Map;

public interface ProductReviewService extends IService<ProductReview> {

    StoreMapper getStoreMapper();

    Page<ProductReview> getDishReviews(Long dishId, int page, int size, Boolean withImage, Integer rating);

    Page<ProductReview> getMyReviews(Long userId, int page, int size);

    List<ProductReview> getOrderByUserReviews(Long orderId, Long userId);

    void submitOrUpdateReview(ProductReview review, Long userId);

    void replyReview(Long reviewId, String content);

    void updateStatus(Long reviewId, int status);

    Map<String, Object> likeReview(Long reviewId, Long userId);

    Map<String, Object> dislikeReview(Long reviewId, Long userId);

    Integer getUserVote(Long reviewId, Long userId);

    Page<ProductReview> getStoreReviews(Long storeId, int page, int size, Integer status,
                                        Integer rating, Boolean withImage, String sortBy);

    Page<ProductReview> getMerchantReviews(Long userId, int page, int size, Long categoryId, Integer rating, String keyword);

    Map<String, Object> getReviewStats(Long userId);

    Double calculateDishRating(Long dishId);

    void updateStoreRating(Long storeId);
}