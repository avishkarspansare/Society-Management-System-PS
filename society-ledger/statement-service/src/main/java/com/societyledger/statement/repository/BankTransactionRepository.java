package com.societyledger.statement.repository;

import com.societyledger.statement.entity.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    Page<BankTransaction> findByStatementIdOrderByTransactionDateDesc(Long statementId, Pageable p);

    Page<BankTransaction> findByStatementIdAndMatchStatus(Long statementId,
                                                          BankTransaction.MatchStatus status, Pageable p);

    List<BankTransaction> findByStatementIdAndMatchStatus(Long statementId,
                                                           BankTransaction.MatchStatus status);

    Optional<BankTransaction> findByIdAndSocietyId(Long id, Long societyId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM BankTransaction t
        WHERE t.societyId = :societyId
          AND t.matchStatus = com.societyledger.statement.entity.BankTransaction.MatchStatus.MATCHED
          AND FUNCTION('YEAR',  t.transactionDate) = :year
          AND FUNCTION('MONTH', t.transactionDate) = :month
        """)
    Optional<BigDecimal> sumCreditsBySocietyIdAndYearAndMonth(Long societyId, int year, int month);

    @Query("""
        SELECT COUNT(DISTINCT t.matchedFlatId)
        FROM BankTransaction t
        WHERE t.societyId = :societyId
          AND t.matchStatus = com.societyledger.statement.entity.BankTransaction.MatchStatus.MATCHED
          AND FUNCTION('YEAR',  t.transactionDate) = :year
          AND FUNCTION('MONTH', t.transactionDate) = :month
        """)
    int countMatchedFlatsBySocietyIdAndYearAndMonth(Long societyId, int year, int month);
}
