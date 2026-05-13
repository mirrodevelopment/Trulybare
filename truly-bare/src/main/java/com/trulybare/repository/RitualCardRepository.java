package com.trulybare.repository;

import com.trulybare.entity.RitualCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RitualCardRepository extends JpaRepository<RitualCard, Long> {
    List<RitualCard> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<RitualCard> findAllByOrderByDisplayOrderAsc();
}
