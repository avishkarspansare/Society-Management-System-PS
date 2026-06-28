package com.societyledger.society.repository;
import com.societyledger.society.entity.Wing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface WingRepository extends JpaRepository<Wing, Long> {
    List<Wing> findBySocietyId(Long societyId);
    Optional<Wing> findByIdAndSocietyId(Long id, Long societyId);
    boolean existsBySocietyIdAndWingNameIgnoreCase(Long societyId, String wingName);
}
