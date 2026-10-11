package com.mediconnect.service;

import com.mediconnect.entity.Appointment;
import com.mediconnect.entity.AppointmentStatus;
import com.mediconnect.entity.Hospital;
import com.mediconnect.entity.Availability;
import com.mediconnect.entity.AppointmentReschedule;
import com.mediconnect.entity.Role;
import com.mediconnect.entity.User;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.AppointmentRescheduleRepository;
import com.mediconnect.repository.AuditLogRepository;
import com.mediconnect.repository.AvailabilityRepository;
import com.mediconnect.repository.DepartmentRepository;
import com.mediconnect.repository.NotificationRepository;
import com.mediconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository appointments;
    @Mock AppointmentRescheduleRepository reschedules;
    @Mock AvailabilityRepository availability;
    @Mock UserRepository users;
    @Mock DepartmentRepository departments;
    @Mock NotificationRepository notifications;
    @Mock AuditLogRepository audits;

    private AppointmentService service;
    private User doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(appointments, reschedules, availability, users, departments, notifications, audits);
        doctor = new User("Doctor", "doctor@example.test", "hash", Role.DOCTOR);
        doctor.setId(10L);
        Hospital hospital = new Hospital("Test Hospital", "test-hospital"); hospital.setId(1L); doctor.setHospital(hospital);
        User patient = new User("Patient", "patient@example.test", "hash", Role.PATIENT);
        patient.setId(20L); patient.setHospital(hospital);
        appointment = new Appointment();
        appointment.setId(30L);
        appointment.setHospital(hospital);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.PENDING);
    }

    @Test
    void allowsPendingAppointmentToBeConfirmedByAssignedDoctor() {
        when(appointments.findById(30L)).thenReturn(Optional.of(appointment));
        when(appointments.save(appointment)).thenReturn(appointment);

        Appointment updated = service.status(30L, doctor, Map.of("status", "CONFIRMED"));

        assertEquals(AppointmentStatus.CONFIRMED, updated.getStatus());
        verify(appointments).save(appointment);
    }

    @Test
    void rejectsMovingCompletedAppointmentBackToConfirmed() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointments.findById(30L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class,
                () -> service.status(30L, doctor, Map.of("status", "CONFIRMED")));
    }

    @Test
    void rejectsDoctorUpdatingAnotherDoctorsAppointment() {
        when(appointments.findById(30L)).thenReturn(Optional.of(appointment));
        User otherDoctor = new User("Other", "other@example.test", "hash", Role.DOCTOR);
        otherDoctor.setId(11L);

        assertThrows(ResponseStatusException.class,
                () -> service.status(30L, otherDoctor, Map.of("status", "CONFIRMED")));
    }

    @Test
    void hospitalAdminAppointmentListUsesOnlyItsHospitalId() {
        Hospital hospital = new Hospital("Hospital A", "hospital-a"); hospital.setId(101L);
        User admin = new User("Admin", "admin@example.test", "hash", Role.ADMIN); admin.setHospital(hospital);
        appointment.setHospital(hospital);
        when(appointments.findAllByHospitalIdOrderByAppointmentDateAscAppointmentTimeAsc(101L)).thenReturn(java.util.List.of(appointment));

        assertEquals(1, service.list(admin, null).size());
        verify(appointments).findAllByHospitalIdOrderByAppointmentDateAscAppointmentTimeAsc(101L);
    }

    @Test
    void rejectsBookingWithDoctorFromAnotherHospitalBeforeSlotLookup() {
        Hospital hospitalA = new Hospital("Hospital A", "hospital-a"); hospitalA.setId(101L);
        Hospital hospitalB = new Hospital("Hospital B", "hospital-b"); hospitalB.setId(202L);
        User patient = new User("Patient", "patient@example.test", "hash", Role.PATIENT); patient.setHospital(hospitalA);
        User doctorB = new User("Doctor B", "doctor-b@example.test", "hash", Role.DOCTOR); doctorB.setId(22L); doctorB.setApproved(true); doctorB.setHospital(hospitalB);
        when(users.findById(22L)).thenReturn(Optional.of(doctorB));

        assertThrows(ResponseStatusException.class, () -> service.book(patient, Map.of(
                "doctor", "22", "appointmentDate", java.time.LocalDate.now().plusDays(1).toString(),
                "appointmentTime", "09:00", "reason", "Consultation")));
        verifyNoInteractions(availability);
    }

    @Test
    void rescheduleMovesSlotAndPersistsOldAndNewTimes() {
        Hospital hospital=new Hospital("Hospital A","hospital-a");hospital.setId(101L);
        User patient=appointment.getPatient();patient.setHospital(hospital);
        doctor.setHospital(hospital);appointment.setHospital(hospital);appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setAppointmentDate(java.time.LocalDate.now().plusDays(1));appointment.setAppointmentTime(java.time.LocalTime.of(9,0));
        java.time.LocalDate targetDate=java.time.LocalDate.now().plusDays(2);
        Availability next=new Availability();next.setHospital(hospital);next.setDoctor(doctor);next.setAvailable(true);
        when(appointments.findById(30L)).thenReturn(Optional.of(appointment));
        when(availability.lockAvailableSlot(10L,targetDate,java.time.LocalTime.of(10,0))).thenReturn(Optional.of(next));
        when(appointments.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(eq(10L),eq(targetDate),eq(java.time.LocalTime.of(10,0)),any())).thenReturn(false);
        when(appointments.save(appointment)).thenReturn(appointment);

        service.reschedule(30L,patient,Map.of("appointmentDate",targetDate.toString(),"appointmentTime","10:00"));

        assertEquals(targetDate,appointment.getAppointmentDate());
        assertEquals(java.time.LocalTime.of(10,0),appointment.getAppointmentTime());
        verify(reschedules).save(argThat((AppointmentReschedule h)->h.getOldTime().equals(java.time.LocalTime.of(9,0))&&h.getNewTime().equals(java.time.LocalTime.of(10,0))));
    }
}
