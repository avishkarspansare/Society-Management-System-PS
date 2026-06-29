package com.societyledger.auth.repository;

import com.societyledger.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByFlatIdAndSocietyId(Long flatId, Long societyId);
    boolean existsByEmail(String email);
    boolean existsByFlatIdAndSocietyId(Long flatId, Long societyId);
}
