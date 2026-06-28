package com.societyledger.statement.repository;
import com.societyledger.statement.entity.PaymentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    boolean existsByFlatIdAndPaymentYearAndPaymentMonthAndPaymentType(Long flatId, int year, int month, String type);
    Page<PaymentRecord> findBySocietyIdOrderByPaymentDateDesc(Long societyId, Pageable p);
    Page<PaymentRecord> findBySocietyIdAndFlatIdOrderByPaymentDateDesc(Long societyId, Long flatId, Pageable p);
    @Query("SELECT COUNT(DISTINCT p.flatId) FROM PaymentRecord p WHERE p.societyId = :societyId AND p.paymentYear = :year AND p.paymentMonth = :month AND p.paymentType = 'MAINTENANCE'")
    int countPaidFlats(Long societyId, int year, int month);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecord p WHERE p.societyId = :societyId AND p.paymentYear = :year AND p.paymentMonth = :month")
    java.math.BigDecimal sumIncomeForMonth(Long societyId, int year, int month);
}
