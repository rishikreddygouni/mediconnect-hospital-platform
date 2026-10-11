package com.mediconnect.controller;

import com.mediconnect.entity.Hospital;
import com.mediconnect.entity.Role;
import com.mediconnect.entity.User;
import com.mediconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTenantTest {
    @Mock UserRepository users;
    @Mock AppointmentRepository appointments;
    @Mock DepartmentRepository departments;
    @Mock MedicalRecordRepository records;
    @Mock PrescriptionRepository prescriptions;
    @Mock ReviewRepository reviews;
    @Mock AuditLogRepository audits;
    private AdminController controller;

    @BeforeEach void setUp() {
        controller = new AdminController(users, appointments, departments, records, prescriptions, reviews, audits);
    }

    @Test void adminUserListingUsesAuthenticatedHospitalId() {
        Hospital hospitalA = new Hospital("Hospital A", "hospital-a"); hospitalA.setId(1L);
        User adminA = new User("Admin A", "admin-a@example.test", "hash", Role.ADMIN); adminA.setHospital(hospitalA);
        when(users.findByHospitalId(1L)).thenReturn(List.of());

        controller.users(adminA);

        verify(users).findByHospitalId(1L);
        verify(users, never()).findAll();
    }

    @Test void adminCannotUpdateUserFromAnotherHospital() {
        Hospital hospitalA = new Hospital("Hospital A", "hospital-a"); hospitalA.setId(1L);
        Hospital hospitalB = new Hospital("Hospital B", "hospital-b"); hospitalB.setId(2L);
        User adminA = new User("Admin A", "admin-a@example.test", "hash", Role.ADMIN); adminA.setHospital(hospitalA);
        User patientB = new User("Patient B", "patient-b@example.test", "hash", Role.PATIENT); patientB.setHospital(hospitalB);
        when(users.findById(9L)).thenReturn(Optional.of(patientB));

        assertThrows(ResponseStatusException.class, () -> controller.update(adminA, 9L, Map.of("name", "Changed")));
        verify(users, never()).save(any());
    }

    @Test void doctorCannotCreateClinicalRecordForPatientInAnotherHospital() {
        Hospital hospitalA = new Hospital("Hospital A", "hospital-a"); hospitalA.setId(1L);
        Hospital hospitalB = new Hospital("Hospital B", "hospital-b"); hospitalB.setId(2L);
        User doctorA = new User("Doctor A", "doctor-a@example.test", "hash", Role.DOCTOR); doctorA.setHospital(hospitalA);
        User patientB = new User("Patient B", "patient-b@example.test", "hash", Role.PATIENT); patientB.setHospital(hospitalB);
        when(users.findById(8L)).thenReturn(Optional.of(patientB));
        MedicalRecordController recordController = new MedicalRecordController(records, users, appointments);

        assertThrows(ResponseStatusException.class, () -> recordController.create(doctorA,
                Map.of("patientId", "8", "appointmentId", "88", "diagnosis", "test")));
        verify(appointments, never()).findById(88L);
        verify(records, never()).save(any());
    }
}
