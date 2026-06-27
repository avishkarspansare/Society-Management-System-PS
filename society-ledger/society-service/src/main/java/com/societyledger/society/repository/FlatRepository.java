package com.societyledger.society.repository;

import com.societyledger.society.entity.Flat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {
    Optional<Flat> findByIdAndSocietyId(Long id, Long societyId);
    Page<Flat> findBySocietyId(Long societyId, Pageable pageable);
    boolean existsByIdAndSocietyId(Long id, Long societyId);
    boolean existsBySocietyIdAndWingIdAndFlatNumber(Long societyId, Long wingId, String flatNumber);
    Optional<Flat> findByPaymentReferenceCode(String paymentReferenceCode);

    @Query("SELECT COUNT(f) FROM Flat f WHERE f.societyId = :societyId")
    int countBySocietyId(Long societyId);

    @Query("SELECT f FROM Flat f LEFT JOIN FETCH f.familyMembers WHERE f.societyId = :societyId")
    List<Flat> findAllBySocietyIdWithFamilyMembers(Long societyId);
}
