package com.trulybare.repository;

import com.trulybare.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Find all addresses for a user
    List<Address> findByUserUserId(Long userId);

    List<Address> findByUserOrderByIsDefaultDesc(com.trulybare.entity.User user);

    // Find default address for a user
    Optional<Address> findByUserAndIsDefaultTrue(com.trulybare.entity.User user);

    // Count addresses for a user
    long countByUser(com.trulybare.entity.User user);

    // Delete all addresses for a user
    void deleteByUser(com.trulybare.entity.User user);
}