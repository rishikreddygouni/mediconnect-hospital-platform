package com.mediconnect.controller;

import com.mediconnect.entity.Hospital;
import com.mediconnect.repository.HospitalRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.entity.Role;
import com.mediconnect.entity.User;
import com.mediconnect.dto.ApiDtos;
import com.mediconnect.entity.SupportTicket;
import com.mediconnect.entity.SupportTicketStatus;
import com.mediconnect.entity.AuditLog;
import com.mediconnect.repository.SupportTicketRepository;
import com.mediconnect.repository.AuditLogRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/platform") @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN')")
public class PlatformController {
    private final HospitalRepository hospitals;
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final SupportTicketRepository tickets;
    private final AuditLogRepository audits;
    public PlatformController(HospitalRepository hospitals, UserRepository users, PasswordEncoder passwords,
                              SupportTicketRepository tickets, AuditLogRepository audits) {
        this.hospitals = hospitals; this.users = users; this.passwords = passwords; this.tickets=tickets; this.audits=audits;
    }

    @GetMapping("/hospitals")
    public List<HospitalController.HospitalView> hospitals() {
        return hospitals.findAll().stream().map(HospitalController.HospitalView::of).toList();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<Hospital> all = hospitals.findAll();
        return Map.of("organizations", all.size(), "active", all.stream().filter(Hospital::isActive).count(),
                "suspended", all.stream().filter(h -> !h.isActive()).count());
    }

    @PostMapping("/hospitals/{id}/admins")
    public ApiDtos.UserView createHospitalAdmin(@PathVariable Long id, @Valid @RequestBody HospitalAdminRequest request) {
        Hospital hospital = hospitals.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        User admin = new User(request.name().trim(), email, passwords.encode(request.temporaryPassword()), Role.ADMIN);
        admin.setHospital(hospital); admin.setApproved(true);
        return ApiDtos.UserView.of(users.save(admin));
    }

    @GetMapping("/support")
    public List<SupportController.TicketView> supportTickets() {
        return tickets.findAll().stream().sorted(java.util.Comparator.comparing(SupportTicket::getCreatedAt).reversed())
                .map(SupportController.TicketView::of).toList();
    }

    @PutMapping("/support/{id}/status")
    public SupportController.TicketView updateTicket(@AuthenticationPrincipal User platformAdmin,@PathVariable Long id,
                                                      @Valid @RequestBody TicketStatusRequest request) {
        SupportTicket ticket=tickets.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Ticket not found"));
        SupportTicketStatus next;
        try { next=SupportTicketStatus.valueOf(request.status().toUpperCase(java.util.Locale.ROOT)); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid ticket status"); }
        ticket.setStatus(next); ticket=tickets.save(ticket);
        AuditLog audit=new AuditLog(platformAdmin,"UPDATE_SUPPORT_TICKET","SupportTicket",ticket.getId(),"Status: "+next);
        audit.setHospital(ticket.getHospital()); audits.save(audit);
        return SupportController.TicketView.of(ticket);
    }

    public record HospitalAdminRequest(@NotBlank String name, @Email @NotBlank String email,
                                       @NotBlank @Size(min=12, max=72) String temporaryPassword) {}
    public record TicketStatusRequest(@NotBlank String status) {}
}
