package com.trulybare.repository;

import com.trulybare.entity.Review;
import com.trulybare.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductProductIdAndReviewStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status);

    List<Review> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByReviewStatusOrderByCreatedAtDesc(ReviewStatus status);

    // Legacy support
    List<Review> findByIsApprovedFalseOrderByCreatedAtDesc();

    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);

    long countByProductProductIdAndReviewStatus(Long productId, ReviewStatus status);

    long countByReviewStatus(ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId AND r.reviewStatus = 'APPROVED'")
    Double getAverageRating(@Param("productId") Long productId);

    // All reviews ordered by date (admin view)
    List<Review> findAllByOrderByCreatedAtDesc();
}