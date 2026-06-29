package com.societyledger.society.repository;
import com.societyledger.society.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByFlatIdAndSocietyId(Long flatId, Long societyId);
    void deleteByIdAndFlatIdAndSocietyId(Long id, Long flatId, Long societyId);
}
