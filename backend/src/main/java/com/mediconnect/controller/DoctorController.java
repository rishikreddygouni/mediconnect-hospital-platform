package com.mediconnect.controller;

import com.mediconnect.dto.ApiDtos;
import com.mediconnect.entity.*;
import com.mediconnect.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/doctors")
public class DoctorController {
    private final UserRepository users; private final AvailabilityRepository availability;
    private final AppointmentRepository appointments; private final ReviewRepository reviews;
    private final HospitalRepository hospitals;
    public DoctorController(UserRepository users, AvailabilityRepository availability, AppointmentRepository appointments,
                            ReviewRepository reviews, HospitalRepository hospitals) {
        this.users=users; this.availability=availability; this.appointments=appointments; this.reviews=reviews; this.hospitals=hospitals;
    }

    @GetMapping public List<ApiDtos.UserView> doctors(@AuthenticationPrincipal User viewer,@RequestParam(defaultValue="") String q,
                                                       @RequestParam(defaultValue="default") String hospitalSlug) {
        Hospital hospital = requireViewerTenant(viewer,hospitalSlug); String term=q.trim().toLowerCase(Locale.ROOT);
        return users.findByHospitalIdAndRoleAndActiveTrueAndApprovedTrueOrderByNameAsc(hospital.getId(), Role.DOCTOR)
                .stream().filter(u -> term.isBlank() || u.getName().toLowerCase(Locale.ROOT).contains(term)
                        || (u.getSpecialization()!=null && u.getSpecialization().toLowerCase(Locale.ROOT).contains(term)))
                .map(ApiDtos.UserView::of).toList();
    }
    @GetMapping("/{id}") public ApiDtos.UserView doctor(@AuthenticationPrincipal User viewer,@PathVariable Long id,@RequestParam(defaultValue="default") String hospitalSlug) {
        return ApiDtos.UserView.of(findDoctor(id,requireViewerTenant(viewer,hospitalSlug).getId()));
    }
    @GetMapping("/{id}/availability") public List<ApiDtos.AvailabilityView> slots(@AuthenticationPrincipal User viewer,@PathVariable Long id,@RequestParam(defaultValue="default") String hospitalSlug) {
        Hospital hospital=requireViewerTenant(viewer,hospitalSlug);findDoctor(id,hospital.getId());
        return availability.findByDoctorIdAndAvailableTrueAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(id,LocalDate.now()).stream()
                .filter(a -> a.getHospital()!=null && a.getHospital().getId().equals(hospital.getId()))
                .map(ApiDtos.AvailabilityView::of).toList();
    }
    @GetMapping("/{id}/reviews") public List<ApiDtos.ReviewView> reviews(@AuthenticationPrincipal User viewer,@PathVariable Long id,@RequestParam(defaultValue="default") String hospitalSlug) {
        Hospital hospital=requireViewerTenant(viewer,hospitalSlug);findDoctor(id,hospital.getId());
        return reviews.findByDoctorIdOrderByCreatedAtDesc(id).stream()
                .filter(r -> r.getHospital()!=null && r.getHospital().getId().equals(hospital.getId()))
                .map(ApiDtos.ReviewView::of).toList();
    }
    @GetMapping("/me/stats") @PreAuthorize("hasRole('DOCTOR')")
    public Map<String,Object> stats(@AuthenticationPrincipal User user) {
        List<Appointment> list=appointments.findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(user.getId());
        LocalDate today=LocalDate.now(); long todayTotal=list.stream().filter(a->a.getAppointmentDate().equals(today)).count();
        long todayCompleted=list.stream().filter(a->a.getAppointmentDate().equals(today)&&a.getStatus()==AppointmentStatus.COMPLETED).count();
        long todayRemaining=list.stream().filter(a->a.getAppointmentDate().equals(today)&&EnumSet.of(AppointmentStatus.PENDING,AppointmentStatus.CONFIRMED).contains(a.getStatus())).count();
        double avg=reviews.findByDoctorIdOrderByCreatedAtDesc(user.getId()).stream().mapToInt(Review::getRating).average().orElse(0);
        Map<String,Object> m=new LinkedHashMap<>(); m.put("total",list.size());m.put("todayTotal",todayTotal);m.put("todayCompleted",todayCompleted);m.put("todayRemaining",todayRemaining);
        m.put("completed",list.stream().filter(a->a.getStatus()==AppointmentStatus.COMPLETED).count());m.put("cancelled",list.stream().filter(a->a.getStatus()==AppointmentStatus.CANCELLED).count());
        m.put("noShow",list.stream().filter(a->a.getStatus()==AppointmentStatus.NO_SHOW).count());m.put("pending",list.stream().filter(a->a.getStatus()==AppointmentStatus.PENDING).count());
        m.put("confirmed",list.stream().filter(a->a.getStatus()==AppointmentStatus.CONFIRMED).count());m.put("patients",list.stream().map(a->a.getPatient().getId()).distinct().count());
        m.put("rating",Math.round(avg*10.0)/10.0);m.put("reviews",reviews.findByDoctorIdOrderByCreatedAtDesc(user.getId()).size()); return m;
    }
    @PutMapping("/{id}/approval") @PreAuthorize("hasRole('ADMIN')")
    public ApiDtos.UserView approval(@AuthenticationPrincipal User admin,@PathVariable Long id,@RequestBody ApprovalBody body) {
        User doctor=users.findById(id).filter(u->u.getHospital()!=null&&admin.getHospital()!=null&&u.getHospital().getId().equals(admin.getHospital().getId()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if(doctor.getRole()!=Role.DOCTOR)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not a doctor");
        doctor.setApproved(body.approved);return ApiDtos.UserView.of(users.save(doctor));
    }
    private User findDoctor(Long id,Long hospitalId) {
        return users.findById(id).filter(u->u.getRole()==Role.DOCTOR&&u.isActive()&&u.isApproved()&&u.getHospital()!=null&&u.getHospital().getId().equals(hospitalId))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found"));
    }
    private Hospital activeHospital(String slug) {
        return hospitals.findBySlugIgnoreCase(slug).filter(Hospital::isActive)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Hospital is unavailable"));
    }
    private Hospital requireViewerTenant(User viewer,String slug) {
        Hospital requested=activeHospital(slug);
        if(viewer==null||viewer.getHospital()==null||!viewer.getHospital().getId().equals(requested.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only browse doctors in your hospital");
        return requested;
    }
    public static class ApprovalBody { public boolean approved; }
}
