package com.societyledger.statement.repository;
import com.societyledger.statement.entity.BankStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, Long> {
    Page<BankStatement> findBySocietyIdOrderByUploadedAtDesc(Long societyId, Pageable p);
    Optional<BankStatement> findByIdAndSocietyId(Long id, Long societyId);
}
