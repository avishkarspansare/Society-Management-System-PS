package com.societyledger.receipt.repository;

import com.societyledger.receipt.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByTransactionId(Long transactionId);
    Page<Receipt> findBySocietyIdAndFlatIdOrderByCreatedAtDesc(Long sid, Long fid, Pageable p);
    Page<Receipt> findBySocietyIdOrderByCreatedAtDesc(Long societyId, Pageable pageable);
    List<Receipt> findBySocietyIdAndYearAndMonth(Long societyId, int year, int month);
    boolean existsByTransactionId(Long transactionId);
    Optional<Receipt> findByIdAndSocietyId(Long id, Long societyId);
}
