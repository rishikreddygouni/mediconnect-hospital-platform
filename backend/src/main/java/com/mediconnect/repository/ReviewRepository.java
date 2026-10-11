package com.mediconnect.repository;
import com.mediconnect.entity.Review; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewRepository extends JpaRepository<Review,Long>{List<Review> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);List<Review> findByDoctorIdOrderByCreatedAtDesc(Long id);List<Review> findByPatientIdOrderByCreatedAtDesc(Long id);Optional<Review> findByAppointmentId(Long id);}
