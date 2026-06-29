package com.societyledger.notification.repository;

import com.societyledger.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findBySocietyIdOrderBySentAtDesc(Long societyId);
    List<NotificationLog> findByFlatIdOrderBySentAtDesc(Long flatId);
}
