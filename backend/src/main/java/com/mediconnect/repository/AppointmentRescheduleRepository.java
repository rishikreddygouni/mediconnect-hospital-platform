package com.mediconnect.repository;

import com.mediconnect.entity.AppointmentReschedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRescheduleRepository extends JpaRepository<AppointmentReschedule,Long> {
    List<AppointmentReschedule> findByAppointmentIdOrderByCreatedAtAsc(Long appointmentId);
    List<AppointmentReschedule> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
}
