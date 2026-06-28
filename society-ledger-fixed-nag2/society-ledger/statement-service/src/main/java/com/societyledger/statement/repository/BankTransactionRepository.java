package com.societyledger.statement.repository;
import com.societyledger.statement.entity.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByStatementIdAndMatchStatus(Long statementId, BankTransaction.MatchStatus status);
    Page<BankTransaction> findBySocietyIdOrderByTransactionDateDesc(Long societyId, Pageable p);
    Page<BankTransaction> findBySocietyIdAndMatchStatus(Long societyId, BankTransaction.MatchStatus status, Pageable p);
    Optional<BankTransaction> findByIdAndSocietyId(Long id, Long societyId);
    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.societyId = :societyId AND t.matchStatus = 'UNMATCHED'")
    long countUnmatched(Long societyId);
}
