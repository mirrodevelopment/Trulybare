package com.trulybare.service;

import com.trulybare.entity.Review;
import com.trulybare.entity.User;
import java.util.List;

public interface ReviewService {

    Review addReview(User user, Long productId, Long orderId, int rating, String comment);

    List<Review> getProductReviews(Long productId);

    List<Review> getUserReviews(User user);

    boolean canUserReviewProduct(User user, Long productId);

    boolean hasUserReviewedProduct(User user, Long productId);

    Double getProductAverageRating(Long productId);

    long getProductReviewCount(Long productId);

    List<Review> getPendingReviews();

    Review approveReview(Long reviewId);

    void deleteReview(Long reviewId);

    Review updateReview(Long reviewId, int rating, String comment);
}