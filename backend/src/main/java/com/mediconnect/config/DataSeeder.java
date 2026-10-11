package com.mediconnect.config;

import com.mediconnect.entity.*;
import com.mediconnect.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(HospitalRepository hospitals, UserRepository users,
                           DepartmentRepository departments, AvailabilityRepository availability,
                           AppointmentRepository appointments, MedicalRecordRepository records,
                           PrescriptionRepository prescriptions, ReviewRepository reviews,
                           AuditLogRepository audits, NotificationRepository notifications, PasswordEncoder encoder,
                           @Value("${app.seed.enabled:false}") boolean enabled,
                           @Value("${app.seed.admin.email}") String adminEmail,
                           @Value("${app.seed.admin.password}") String adminPassword,
                           @Value("${app.seed.doctor.email}") String doctorEmail,
                           @Value("${app.seed.doctor.password}") String doctorPassword,
                           @Value("${app.seed.patient.email}") String patientEmail,
                           @Value("${app.seed.patient.password}") String patientPassword,
                           @Value("${app.platform-admin.email:}") String platformAdminEmail,
                           @Value("${app.platform-admin.password:}") String platformAdminPassword) {
        return args -> {
            Hospital hospital = hospitals.findBySlugIgnoreCase("default").orElseGet(() ->
                    hospitals.save(new Hospital("MediConnect Demo Hospital", "default")));
            backfillTenantOwnership(hospital, users, departments, availability, appointments, records,
                    prescriptions, reviews, audits, notifications);
            if (platformAdminEmail.isBlank() != platformAdminPassword.isBlank()) {
                throw new IllegalStateException("Set both PLATFORM_ADMIN_EMAIL and PLATFORM_ADMIN_PASSWORD to bootstrap a platform administrator");
            }
            if (!platformAdminEmail.isBlank() && (platformAdminPassword.length() < 12 || platformAdminPassword.length() > 72)) {
                throw new IllegalStateException("PLATFORM_ADMIN_PASSWORD must be between 12 and 72 characters");
            }
            if (!platformAdminEmail.isBlank() && users.findByEmailIgnoreCase(platformAdminEmail).isPresent()
                    && users.findByEmailIgnoreCase(platformAdminEmail).orElseThrow().getRole() != Role.PLATFORM_SUPER_ADMIN) {
                throw new IllegalStateException("PLATFORM_ADMIN_EMAIL is already assigned to a non-platform account");
            }
            if (!platformAdminEmail.isBlank() && users.findByEmailIgnoreCase(platformAdminEmail).isEmpty()) {
                User platformAdmin = new User("MediConnect Platform Admin", platformAdminEmail.trim().toLowerCase(java.util.Locale.ROOT),
                        encoder.encode(platformAdminPassword), Role.PLATFORM_SUPER_ADMIN);
                platformAdmin.setApproved(true);
                users.save(platformAdmin);
            }
            if (!enabled) return;
            if (adminPassword.isBlank() || doctorPassword.isBlank() || patientPassword.isBlank()) {
                throw new IllegalStateException("Set unique SEED_ADMIN_PASSWORD, SEED_DOCTOR_PASSWORD, and SEED_PATIENT_PASSWORD values when demo seeding is enabled");
            }
            seedAccounts(hospital, users, departments, availability, encoder,
                    adminEmail, adminPassword, doctorEmail, doctorPassword, patientEmail, patientPassword);
        };
    }

    void backfillTenantOwnership(Hospital hospital, UserRepository users, DepartmentRepository departments,
                                 AvailabilityRepository availability, AppointmentRepository appointments,
                                 MedicalRecordRepository records, PrescriptionRepository prescriptions,
                                 ReviewRepository reviews, AuditLogRepository audits, NotificationRepository notifications) {
        users.saveAll(users.findAll().stream().filter(u -> u.getHospital() == null && u.getRole() != Role.PLATFORM_SUPER_ADMIN)
                .peek(u -> u.setHospital(hospital)).toList());
        departments.saveAll(departments.findAll().stream().filter(d -> d.getHospital() == null).peek(d -> d.setHospital(hospital)).toList());
        availability.saveAll(availability.findAll().stream().filter(a -> a.getHospital() == null).peek(a -> a.setHospital(hospital)).toList());
        appointments.saveAll(appointments.findAll().stream().filter(a -> a.getHospital() == null).peek(a -> a.setHospital(hospital)).toList());
        records.saveAll(records.findAll().stream().filter(r -> r.getHospital() == null).peek(r -> r.setHospital(hospital)).toList());
        prescriptions.saveAll(prescriptions.findAll().stream().filter(p -> p.getHospital() == null).peek(p -> p.setHospital(hospital)).toList());
        reviews.saveAll(reviews.findAll().stream().filter(r -> r.getHospital() == null).peek(r -> r.setHospital(hospital)).toList());
        notifications.saveAll(notifications.findAll().stream().filter(n -> n.getHospital() == null && n.getUser().getHospital() != null)
                .peek(n -> n.setHospital(n.getUser().getHospital())).toList());
        audits.saveAll(audits.findAll().stream().filter(a -> a.getHospital() == null && a.getActor() != null)
                .peek(a -> a.setHospital(a.getActor().getHospital() == null ? hospital : a.getActor().getHospital())).toList());
    }

    private void seedAccounts(Hospital hospital, UserRepository users, DepartmentRepository departments,
                              AvailabilityRepository availability, PasswordEncoder encoder,
                              String adminEmail, String adminPassword, String doctorEmail, String doctorPassword,
                              String patientEmail, String patientPassword) {
        User admin = users.findByEmailIgnoreCase(adminEmail).orElseGet(() ->
                users.save(user(hospital, "MediConnect Admin", adminEmail, adminPassword, Role.ADMIN, encoder)));
        admin.setHospital(hospital); admin.setApproved(true); admin.setActive(true); users.save(admin);

        User doctor = users.findByEmailIgnoreCase(doctorEmail).orElseGet(() -> {
            User d = user(hospital, "Dr. Priya Sharma", doctorEmail, doctorPassword, Role.DOCTOR, encoder);
            d.setSpecialization("Cardiologist"); d.setQualification("MBBS, MD"); d.setExperience(12);
            d.setConsultationFee(new BigDecimal("700")); d.setApproved(true); return users.save(d);
        });
        doctor.setHospital(hospital); doctor.setApproved(true); doctor.setActive(true); users.save(doctor);

        User patient = users.findByEmailIgnoreCase(patientEmail).orElseGet(() -> {
            User p = user(hospital, "Rahul Kumar", patientEmail, patientPassword, Role.PATIENT, encoder);
            p.setPhone("9876543210"); p.setGender("Male"); p.setBloodGroup("O+"); p.setAddress("Hyderabad");
            p.setEmergencyContact("9876500000"); return users.save(p);
        });
        patient.setHospital(hospital); patient.setActive(true); patient.setApproved(true); users.save(patient);

        Department department = departments.findByNameIgnoreCaseAndHospitalId("Cardiology", hospital.getId()).orElseGet(() -> {
            Department d = new Department("Cardiology", "Heart and cardiovascular care", "Block A, Floor 2");
            d.setHospital(hospital); return departments.save(d);
        });
        department.setHospital(hospital);
        LocalDate date = LocalDate.now().plusDays(1);
        for (String value : new String[]{"09:00", "10:00", "11:00", "14:00", "15:00"}) {
            LocalTime start = LocalTime.parse(value);
            if (availability.findByDoctorIdAndDateAndStartTime(doctor.getId(), date, start).isEmpty()) {
                Availability slot = new Availability(); slot.setDoctor(doctor); slot.setHospital(hospital);
                slot.setDate(date); slot.setStartTime(start); slot.setEndTime(start.plusHours(1));
                slot.setAvailable(true); availability.save(slot);
            }
        }
    }

    private User user(Hospital hospital, String name, String email, String password, Role role, PasswordEncoder encoder) {
        User user = new User(name, email, encoder.encode(password), role);
        user.setHospital(hospital);
        return user;
    }
}
