package com.societyledger.query.repository;
import com.societyledger.query.entity.QueryResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface QueryResponseRepository extends JpaRepository<QueryResponseEntity, Long> {
    List<QueryResponseEntity> findByQueryIdOrderByCreatedAtAsc(Long queryId);
}
