package com.trulybare.repository;

import com.trulybare.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Sales analytics: product-wise sales between two dates
    @Query("SELECT oi.product.name, SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.createdAt BETWEEN :start AND :end " +
           "GROUP BY oi.product.productId, oi.product.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findProductSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Total revenue between two dates
    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi " +
           "WHERE oi.order.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal findTotalRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Total orders count between two dates
    @Query("SELECT COUNT(DISTINCT oi.order.orderId) FROM OrderItem oi " +
           "WHERE oi.order.createdAt BETWEEN :start AND :end")
    Long findTotalOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
