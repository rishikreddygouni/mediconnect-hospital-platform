package com.mediconnect.repository;
import com.mediconnect.entity.MedicalRecord; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Long>{List<MedicalRecord> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long id);List<MedicalRecord> findByDoctorIdOrderByCreatedAtDesc(Long id);}
