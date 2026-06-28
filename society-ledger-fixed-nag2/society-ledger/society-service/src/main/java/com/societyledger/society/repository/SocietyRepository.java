package com.societyledger.society.repository;

import com.societyledger.society.entity.Society;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SocietyRepository extends JpaRepository<Society, Long> {
    Optional<Society> findByIdAndIsActiveTrue(Long id);
    boolean existsByRegistrationNumber(String registrationNumber);
}
