package com.mediconnect.repository;
import com.mediconnect.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDoctorIdOrderByDateAscStartTimeAsc(Long doctorId);
    List<Availability> findByDoctorIdAndAvailableTrueAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(Long doctorId, LocalDate date);
    Optional<Availability> findByDoctorIdAndDateAndStartTimeAndAvailableTrue(Long doctorId, LocalDate date, LocalTime startTime);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from Availability a where a.doctor.id = :doctorId and a.date = :date and a.startTime = :startTime and a.available = true")
    Optional<Availability> lockAvailableSlot(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, @Param("startTime") LocalTime startTime);
    Optional<Availability> findByDoctorIdAndDateAndStartTime(Long doctorId, LocalDate date, LocalTime startTime);
    @org.springframework.data.jpa.repository.Query("select (count(a) > 0) from Availability a where a.doctor.id = :doctorId and a.date = :date and a.startTime < :endTime and a.endTime > :startTime")
    boolean existsOverlappingSession(@Param("doctorId") Long doctorId, @Param("date") LocalDate date,
                                     @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
