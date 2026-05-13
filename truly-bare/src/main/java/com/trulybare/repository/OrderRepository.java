package com.trulybare.repository;

import com.trulybare.entity.Order;
import com.trulybare.entity.User;
import com.trulybare.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find all orders for a user (no pagination)
    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // Find orders for a user WITH pagination
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find orders by status (sorted)
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    // Find orders by status (all)
    List<Order> findByStatus(OrderStatus status);

    // Count orders by status
    long countByStatus(OrderStatus status);
}