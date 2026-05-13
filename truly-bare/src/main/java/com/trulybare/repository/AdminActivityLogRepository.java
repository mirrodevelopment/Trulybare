package com.trulybare.repository;

import com.trulybare.entity.AdminActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, Long> {
    List<AdminActivityLog> findAllByOrderByCreatedAtDesc();
    List<AdminActivityLog> findByActionTypeNotOrderByCreatedAtDesc(String actionType);
    List<AdminActivityLog> findByActionTypeNotAndCreatedAtBetweenOrderByCreatedAtDesc(String actionType, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<AdminActivityLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);
    List<AdminActivityLog> findByAdminIdOrderByCreatedAtDesc(Long adminId);
}
