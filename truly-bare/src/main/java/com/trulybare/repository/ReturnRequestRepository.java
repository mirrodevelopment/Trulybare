package com.trulybare.repository;

import com.trulybare.entity.ReturnRequest;
import com.trulybare.entity.User;
import com.trulybare.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUserOrderByCreatedAtDesc(User user);

    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnStatus status);

    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    Optional<ReturnRequest> findByOrder_OrderIdAndUser(Long orderId, User user);

    boolean existsByOrder_OrderIdAndUser(Long orderId, User user);

    long countByStatus(ReturnStatus status);
}
