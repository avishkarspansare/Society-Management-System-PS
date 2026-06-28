package com.societyledger.statement.repository;
import com.societyledger.statement.entity.UnmatchedTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface UnmatchedTransactionRepository extends JpaRepository<UnmatchedTransaction, Long> {
    Page<UnmatchedTransaction> findBySocietyIdAndResolvedFalseOrderByCreatedAtDesc(Long societyId, Pageable p);
    Optional<UnmatchedTransaction> findByTransactionId(Long transactionId);
}
