package com.mediconnect.service;

import com.mediconnect.dto.ApiDtos;
import com.mediconnect.entity.Hospital;
import com.mediconnect.entity.Role;
import com.mediconnect.entity.User;
import com.mediconnect.repository.HospitalRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository users;
    private final HospitalRepository hospitals;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String defaultHospitalSlug;

    public AuthService(UserRepository users, HospitalRepository hospitals, PasswordEncoder encoder, JwtService jwt,
                       @Value("${app.default-hospital.slug:default}") String defaultHospitalSlug) {
        this.users = users; this.hospitals = hospitals; this.encoder = encoder; this.jwt = jwt;
        this.defaultHospitalSlug = defaultHospitalSlug;
    }

    public ApiDtos.AuthResponse register(Map<String,Object> body) {
        String name = value(body, "name"), email = value(body, "email").toLowerCase(), password = value(body, "password");
        if (name.isBlank() || email.isBlank() || password.isBlank()) throw bad("Name, email, and password are required");
        if (password.length() < 8) throw bad("Password must contain at least 8 characters");
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        Role role = parsePublicRole(value(body, "role"));
        String slug = value(body, "hospitalSlug");
        if (slug.isBlank()) slug = defaultHospitalSlug;
        Hospital hospital = hospitals.findBySlugIgnoreCase(slug)
                .filter(Hospital::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital is unavailable"));
        User user = new User(name, email, encoder.encode(password), role);
        user.setHospital(hospital);
        applyProfile(user, body);
        users.save(user);
        boolean pending = role == Role.DOCTOR && !user.isApproved();
        String token = pending ? null : jwt.generate(user.getId(), user.getRole().name());
        return new ApiDtos.AuthResponse(token, ApiDtos.UserView.of(user), pending);
    }

    public ApiDtos.AuthResponse login(Map<String,Object> body) {
        String email = value(body, "email").toLowerCase();
        User user = users.findByEmailIgnoreCase(email).orElseThrow(AuthService::unauth);
        if (!encoder.matches(value(body, "password"), user.getPassword())) throw unauth();
        if (!user.isActive()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        if (user.getRole() != Role.PLATFORM_SUPER_ADMIN && (user.getHospital() == null || !user.getHospital().isActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hospital workspace is inactive");
        }
        if (user.getRole() == Role.DOCTOR && !user.isApproved()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor account is awaiting approval");
        return new ApiDtos.AuthResponse(jwt.generate(user.getId(), user.getRole().name()), ApiDtos.UserView.of(user), false);
    }

    public ApiDtos.UserView updateProfile(User user, Map<String,Object> body) {
        applyProfile(user, body); users.save(user); return ApiDtos.UserView.of(user);
    }

    private void applyProfile(User user, Map<String,Object> body) {
        if (body.get("name") != null && !value(body, "name").isBlank()) user.setName(value(body, "name"));
        if (body.get("phone") != null) user.setPhone(value(body, "phone"));
        if (body.get("dateOfBirth") != null && !value(body, "dateOfBirth").isBlank()) user.setDateOfBirth(LocalDate.parse(value(body, "dateOfBirth")));
        if (body.get("gender") != null) user.setGender(value(body, "gender"));
        if (body.get("address") != null) user.setAddress(value(body, "address"));
        if (body.get("bloodGroup") != null) user.setBloodGroup(value(body, "bloodGroup"));
        if (body.get("allergies") != null) user.setAllergies(value(body, "allergies"));
        if (body.get("emergencyContact") != null) user.setEmergencyContact(value(body, "emergencyContact"));
        if (body.get("specialization") != null) user.setSpecialization(value(body, "specialization"));
        if (body.get("qualification") != null) user.setQualification(value(body, "qualification"));
        if (body.get("licenseNumber") != null) user.setLicenseNumber(value(body, "licenseNumber"));
        if (body.get("experience") instanceof Number n) user.setExperience(n.intValue());
        if (body.get("consultationFee") instanceof Number n) user.setConsultationFee(new BigDecimal(n.toString()));
    }

    private static Role parsePublicRole(String role) {
        return "DOCTOR".equalsIgnoreCase(role) ? Role.DOCTOR : Role.PATIENT;
    }
    private static String value(Map<String,Object> body, String key) {
        Object value = body.get(key); return value == null ? "" : String.valueOf(value).trim();
    }
    private static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private static ResponseStatusException unauth() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"); }
}
