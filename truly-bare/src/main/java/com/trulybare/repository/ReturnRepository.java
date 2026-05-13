package com.trulybare.repository;

import com.trulybare.entity.Order;
import com.trulybare.entity.Return;
import com.trulybare.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {

    // Find all returns for a user
    List<Return> findByUserOrderByCreatedAtDesc(com.trulybare.entity.User user);

    // Find return by order
    Optional<Return> findByOrder(Order order);

    // Find returns by status (for admin)
    List<Return> findByStatusOrderByCreatedAtDesc(ReturnStatus status);

    // Find pending returns (for admin dashboard)
    List<Return> findByStatus(ReturnStatus status);

    // Check if return already exists for an order
    boolean existsByOrder(Order order);

    // Count returns by status
    long countByStatus(ReturnStatus status);
}