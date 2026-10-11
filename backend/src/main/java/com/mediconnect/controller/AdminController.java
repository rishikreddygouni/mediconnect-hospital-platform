package com.mediconnect.controller;

import com.mediconnect.dto.ApiDtos;
import com.mediconnect.entity.*;
import com.mediconnect.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository users; private final AppointmentRepository appointments;
    private final DepartmentRepository departments; private final MedicalRecordRepository records;
    private final PrescriptionRepository prescriptions; private final ReviewRepository reviews;
    private final AuditLogRepository audits;
    public AdminController(UserRepository users, AppointmentRepository appointments, DepartmentRepository departments,
                           MedicalRecordRepository records, PrescriptionRepository prescriptions,
                           ReviewRepository reviews, AuditLogRepository audits) {
        this.users=users; this.appointments=appointments; this.departments=departments; this.records=records;
        this.prescriptions=prescriptions; this.reviews=reviews; this.audits=audits;
    }

    @GetMapping("/users") public List<ApiDtos.UserView> users(@AuthenticationPrincipal User admin) {
        return users.findByHospitalId(hospitalId(admin)).stream().sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))).map(ApiDtos.UserView::of).toList();
    }
    @GetMapping("/patients") public List<ApiDtos.UserView> patients(@AuthenticationPrincipal User admin) {
        return users.findByHospitalIdAndRole(hospitalId(admin), Role.PATIENT).stream().sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER)).map(ApiDtos.UserView::of).toList();
    }
    @GetMapping("/doctors") public List<ApiDtos.UserView> doctors(@AuthenticationPrincipal User admin) {
        return users.findByHospitalIdAndRole(hospitalId(admin), Role.DOCTOR).stream().sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER)).map(ApiDtos.UserView::of).toList();
    }
    @GetMapping("/appointments") public List<ApiDtos.AppointmentView> appointments(@AuthenticationPrincipal User admin) {
        return appointments.findAllByHospitalIdOrderByAppointmentDateAscAppointmentTimeAsc(hospitalId(admin)).stream().map(ApiDtos.AppointmentView::of).toList();
    }
    @GetMapping("/records") public List<ApiDtos.MedicalRecordView> records(@AuthenticationPrincipal User admin) {
        return records.findByHospitalIdOrderByCreatedAtDesc(hospitalId(admin)).stream().map(ApiDtos.MedicalRecordView::of).toList();
    }
    @GetMapping("/prescriptions") public List<ApiDtos.PrescriptionView> prescriptions(@AuthenticationPrincipal User admin) {
        return prescriptions.findByHospitalIdOrderByCreatedAtDesc(hospitalId(admin)).stream().map(ApiDtos.PrescriptionView::of).toList();
    }
    @GetMapping("/reviews") public List<ApiDtos.ReviewView> reviews(@AuthenticationPrincipal User admin) {
        return reviews.findByHospitalIdOrderByCreatedAtDesc(hospitalId(admin)).stream().map(ApiDtos.ReviewView::of).toList();
    }
    @GetMapping("/audit") public List<ApiDtos.AuditView> audit(@AuthenticationPrincipal User admin) {
        return audits.findTop200ByHospitalIdOrderByCreatedAtDesc(hospitalId(admin)).stream().map(ApiDtos.AuditView::of).toList();
    }

    @PutMapping("/users/{id}") public ApiDtos.UserView update(@AuthenticationPrincipal User admin, @PathVariable Long id, @RequestBody Map<String,Object> body) {
        User user = users.findById(id).filter(target -> target.getHospital() != null && target.getHospital().getId().equals(hospitalId(admin)))
                .orElseThrow(() -> notFound("User not found"));
        if (body.get("isActive") != null) user.setActive(Boolean.parseBoolean(String.valueOf(body.get("isActive"))));
        if (body.get("isApproved") != null && user.getRole() == Role.DOCTOR) user.setApproved(Boolean.parseBoolean(String.valueOf(body.get("isApproved"))));
        if (body.get("role") != null) try {
            Role role = Role.valueOf(String.valueOf(body.get("role")).toUpperCase(Locale.ROOT));
            if (role == Role.PLATFORM_SUPER_ADMIN) throw bad("Hospital administrators cannot grant platform roles");
            user.setRole(role);
        } catch (IllegalArgumentException e) { throw bad("Invalid role"); }
        if (body.get("name") != null && !value(body,"name").isBlank()) user.setName(value(body,"name"));
        if (body.get("phone") != null) user.setPhone(value(body,"phone"));
        if (body.get("dateOfBirth") != null && !value(body,"dateOfBirth").isBlank()) user.setDateOfBirth(LocalDate.parse(value(body,"dateOfBirth")));
        if (body.get("gender") != null) user.setGender(value(body,"gender"));
        if (body.get("address") != null) user.setAddress(value(body,"address"));
        if (body.get("bloodGroup") != null) user.setBloodGroup(value(body,"bloodGroup"));
        if (body.get("allergies") != null) user.setAllergies(value(body,"allergies"));
        if (body.get("emergencyContact") != null) user.setEmergencyContact(value(body,"emergencyContact"));
        if (body.get("specialization") != null) user.setSpecialization(value(body,"specialization"));
        if (body.get("qualification") != null) user.setQualification(value(body,"qualification"));
        if (body.get("licenseNumber") != null) user.setLicenseNumber(value(body,"licenseNumber"));
        if (body.get("experience") instanceof Number n) user.setExperience(n.intValue());
        if (body.get("consultationFee") instanceof Number n) user.setConsultationFee(new BigDecimal(n.toString()));
        return ApiDtos.UserView.of(users.save(user));
    }

    @GetMapping("/dashboard") public Map<String,Object> dashboard(@AuthenticationPrincipal User admin) {
        Long hospitalId = hospitalId(admin); Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("users", users.findByHospitalId(hospitalId).size());
        stats.put("doctors", users.countByHospitalIdAndRole(hospitalId, Role.DOCTOR));
        stats.put("patients", users.countByHospitalIdAndRole(hospitalId, Role.PATIENT));
        stats.put("departments", departments.findByHospitalIdAndActiveTrueOrderByNameAsc(hospitalId).size());
        stats.put("total", appointments.countByHospitalId(hospitalId));
        stats.put("pending", appointments.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.PENDING));
        stats.put("confirmed", appointments.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.CONFIRMED));
        stats.put("completed", appointments.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.COMPLETED));
        stats.put("cancelled", appointments.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.CANCELLED));
        stats.put("noShow", appointments.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.NO_SHOW));
        stats.put("medicalRecords", records.findByHospitalIdOrderByCreatedAtDesc(hospitalId).size());
        stats.put("prescriptions", prescriptions.findByHospitalIdOrderByCreatedAtDesc(hospitalId).size());
        stats.put("reviews", reviews.findByHospitalIdOrderByCreatedAtDesc(hospitalId).size());
        return stats;
    }
    private static Long hospitalId(User admin) { if (admin == null || admin.getHospital() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Hospital membership required"); return admin.getHospital().getId(); }
    private static String value(Map<String,Object> body,String key) { Object v=body.get(key); return v==null?"":String.valueOf(v).trim(); }
    private static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,message); }
    private static ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND,message); }
}
