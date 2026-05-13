package com.trulybare.repository;

import com.trulybare.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find all active categories
    List<Category> findByIsActiveTrue();

    // Find category by name
    Optional<Category> findByName(String name);

    // Check if category exists by name
    boolean existsByName(String name);

    // Find categories with product count (for admin dashboard)
    List<Category> findAllByOrderByDisplayOrderAsc();
}