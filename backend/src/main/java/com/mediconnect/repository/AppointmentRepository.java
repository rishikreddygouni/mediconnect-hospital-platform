package com.mediconnect.repository;
import com.mediconnect.entity.Appointment;
import com.mediconnect.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(Long doctorId);
    List<Appointment> findAllByOrderByAppointmentDateAscAppointmentTimeAsc();
    List<Appointment> findAllByHospitalIdOrderByAppointmentDateAscAppointmentTimeAsc(Long hospitalId);
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(Long doctorId, LocalDate date, LocalTime time, List<AppointmentStatus> statuses);
    long countByStatus(AppointmentStatus status);
    long countByHospitalIdAndStatus(Long hospitalId, AppointmentStatus status);
    long countByHospitalId(Long hospitalId);
}
