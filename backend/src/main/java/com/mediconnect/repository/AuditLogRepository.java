package com.mediconnect.repository;
import com.mediconnect.entity.AuditLog; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{List<AuditLog> findTop200ByOrderByCreatedAtDesc();List<AuditLog> findTop200ByHospitalIdOrderByCreatedAtDesc(Long hospitalId);}
