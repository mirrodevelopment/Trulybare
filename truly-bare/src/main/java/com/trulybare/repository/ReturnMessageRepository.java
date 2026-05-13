package com.trulybare.repository;

import com.trulybare.entity.ReturnMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnMessageRepository extends JpaRepository<ReturnMessage, Long> {
    List<ReturnMessage> findByReturnRequest_IdOrderByCreatedAtAsc(Long returnRequestId);
}
