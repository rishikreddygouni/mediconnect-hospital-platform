package com.mediconnect.repository;
import com.mediconnect.entity.Prescription; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface PrescriptionRepository extends JpaRepository<Prescription,Long>{List<Prescription> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long id);List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long id);}
