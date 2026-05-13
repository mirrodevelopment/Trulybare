package com.trulybare.repository;

import com.trulybare.entity.Vlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VlogRepository extends JpaRepository<Vlog, Long> {
    List<Vlog> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}
