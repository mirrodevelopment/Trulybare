package com.trulybare.service.impl;

import com.trulybare.entity.Review;
import com.trulybare.entity.User;
import com.trulybare.entity.Product;
import com.trulybare.entity.Order;
import com.trulybare.enums.ReviewStatus;
import com.trulybare.repository.ReviewRepository;
import com.trulybare.repository.ProductRepository;
import com.trulybare.repository.OrderRepository;
import com.trulybare.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public Review addReview(User user, Long productId, Long orderId, int rating, String comment) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId).orElse(null);
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsVerifiedPurchase(order != null);
        // New reviews start as PENDING — admin must approve via admin panel
        review.setReviewStatus(ReviewStatus.PENDING);
        review.setIsApproved(false);

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getProductReviews(Long productId) {
        // Only return APPROVED reviews on the public storefront
        return reviewRepository.findByProductProductIdAndReviewStatusOrderByCreatedAtDesc(
                productId, ReviewStatus.APPROVED);
    }

    @Override
    public List<Review> getUserReviews(User user) {
        return reviewRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    }

    @Override
    public boolean canUserReviewProduct(User user, Long productId) {
        return true;
    }

    @Override
    public boolean hasUserReviewedProduct(User user, Long productId) {
        return reviewRepository.existsByUserUserIdAndProductProductId(user.getUserId(), productId);
    }

    @Override
    public Double getProductAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public long getProductReviewCount(Long productId) {
        return reviewRepository.countByProductProductIdAndReviewStatus(productId, ReviewStatus.APPROVED);
    }

    @Override
    public List<Review> getPendingReviews() {
        return reviewRepository.findByReviewStatusOrderByCreatedAtDesc(ReviewStatus.PENDING);
    }

    @Override
    public Review approveReview(Long reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setReviewStatus(ReviewStatus.APPROVED);
        review.setIsApproved(true);
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Review updateReview(Long reviewId, int rating, String comment) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }
}
