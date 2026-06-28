package com.societyledger.society.repository;
import com.societyledger.society.entity.Flat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {
    Page<Flat> findBySocietyId(Long societyId, Pageable pageable);
    List<Flat> findBySocietyId(Long societyId);
    Optional<Flat> findByIdAndSocietyId(Long id, Long societyId);
    Optional<Flat> findByPaymentReferenceCode(String code);
    boolean existsByIdAndSocietyId(Long id, Long societyId);
    boolean existsBySocietyIdAndWingIdAndFlatNumber(Long societyId, Long wingId, String flatNumber);
    int countBySocietyId(Long societyId);
}
