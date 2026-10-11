package com.mediconnect.repository;
import com.mediconnect.entity.Notification; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<Notification,Long>{List<Notification> findByUserIdOrderByCreatedAtDesc(Long id);long countByUserIdAndReadFlagFalse(Long id);}
