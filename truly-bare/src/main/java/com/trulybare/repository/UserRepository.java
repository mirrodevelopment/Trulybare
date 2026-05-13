package com.trulybare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<com.trulybare.entity.User, Long> {
    Optional<com.trulybare.entity.User> findByEmail(String email);
    Optional<com.trulybare.entity.User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}