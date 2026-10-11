package com.mediconnect.controller;

import com.mediconnect.entity.Hospital;
import com.mediconnect.repository.HospitalRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {
    private final HospitalRepository hospitals;
    public HospitalController(HospitalRepository hospitals) { this.hospitals = hospitals; }

    @GetMapping
    public List<HospitalView> activeHospitals() {
        return hospitals.findByActiveTrueOrderByNameAsc().stream().map(HospitalView::of).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN')")
    public HospitalView create(@Valid @RequestBody HospitalRequest request) {
        String slug = slugify(request.slug().isBlank() ? request.name() : request.slug());
        if (hospitals.existsBySlugIgnoreCase(slug)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Hospital slug already exists");
        Hospital hospital = new Hospital(request.name().trim(), slug);
        return HospitalView.of(hospitals.save(hospital));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN')")
    public HospitalView setStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        Hospital hospital = hospitals.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        hospital.setActive(request.active());
        return HospitalView.of(hospitals.save(hospital));
    }

    private static String slugify(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
    public record HospitalRequest(@NotBlank String name, String slug) {
        public HospitalRequest { if (slug == null) slug = ""; }
    }
    public record StatusRequest(boolean active) {}
    public record HospitalView(Long id, String name, String slug, boolean active, String subscriptionStatus) {
        static HospitalView of(Hospital hospital) {
            return new HospitalView(hospital.getId(), hospital.getName(), hospital.getSlug(), hospital.isActive(), hospital.getSubscriptionStatus());
        }
    }
}
