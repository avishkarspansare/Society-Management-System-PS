package com.societyledger.receipt.repository;
import com.societyledger.receipt.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Page<Receipt> findBySocietyIdOrderByGeneratedAtDesc(Long societyId, Pageable p);
    Page<Receipt> findBySocietyIdAndFlatIdOrderByGeneratedAtDesc(Long societyId, Long flatId, Pageable p);
    Optional<Receipt> findByIdAndSocietyId(Long id, Long societyId);
    Optional<Receipt> findByPaymentId(Long paymentId);
    boolean existsByPaymentId(Long paymentId);
    long countBySocietyId(Long societyId);
}
