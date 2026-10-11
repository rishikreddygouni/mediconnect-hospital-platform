package com.mediconnect.controller;

import com.mediconnect.dto.ApiDtos;
import com.mediconnect.entity.Department;
import com.mediconnect.entity.User;
import com.mediconnect.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController @RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentRepository repo;
    public DepartmentController(DepartmentRepository repo) { this.repo = repo; }

    @GetMapping
    public List<ApiDtos.DepartmentView> all(@AuthenticationPrincipal User user) {
        requireHospital(user);
        return repo.findByHospitalIdAndActiveTrueOrderByNameAsc(user.getHospital().getId())
                .stream().map(ApiDtos.DepartmentView::of).toList();
    }

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ApiDtos.DepartmentView create(@AuthenticationPrincipal User user, @RequestBody Department department) {
        requireHospital(user);
        if (department.getName() == null || department.getName().isBlank()) throw bad("Department name is required");
        String name = department.getName().trim();
        if (repo.existsByNameIgnoreCaseAndHospitalId(name, user.getHospital().getId())) throw conflict("Department already exists");
        Department saved = new Department(name, department.getDescription(), department.getLocation());
        saved.setContactNumber(department.getContactNumber()); saved.setActive(department.isActive()); saved.setHospital(user.getHospital());
        return ApiDtos.DepartmentView.of(repo.save(saved));
    }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiDtos.DepartmentView update(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody Department body) {
        Department current = findInHospital(user, id);
        if (body.getName() == null || body.getName().isBlank()) throw bad("Department name is required");
        String name = body.getName().trim();
        if (!current.getName().equalsIgnoreCase(name) && repo.existsByNameIgnoreCaseAndHospitalId(name, user.getHospital().getId())) throw conflict("Department already exists");
        current.setName(name); current.setDescription(body.getDescription()); current.setLocation(body.getLocation());
        current.setContactNumber(body.getContactNumber()); current.setActive(body.isActive());
        return ApiDtos.DepartmentView.of(repo.save(current));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        Department department = findInHospital(user, id); department.setActive(false); repo.save(department);
    }

    private Department findInHospital(User user, Long id) {
        requireHospital(user);
        Department department = repo.findById(id).orElseThrow(() -> notFound("Department not found"));
        if (department.getHospital() == null || !department.getHospital().getId().equals(user.getHospital().getId())) throw notFound("Department not found");
        return department;
    }
    private static void requireHospital(User user) { if (user == null || user.getHospital() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hospital membership required"); }
    private static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private static ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
    private static ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
}
