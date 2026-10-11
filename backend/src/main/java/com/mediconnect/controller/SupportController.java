package com.mediconnect.controller;

import com.mediconnect.entity.*;
import com.mediconnect.repository.AuditLogRepository;
import com.mediconnect.repository.SupportTicketRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController @RequestMapping("/api/support/tickets")
public class SupportController {
    private final SupportTicketRepository tickets;
    private final AuditLogRepository audits;
    public SupportController(SupportTicketRepository tickets, AuditLogRepository audits) { this.tickets=tickets; this.audits=audits; }

    @GetMapping public List<TicketView> list(@AuthenticationPrincipal User user) {
        if (user.getRole()==Role.PLATFORM_SUPER_ADMIN) throw forbidden("Use platform support operations");
        if (user.getHospital()==null) throw forbidden("Hospital membership required");
        List<SupportTicket> rows=user.getRole()==Role.ADMIN
                ? tickets.findByHospitalIdOrderByCreatedAtDesc(user.getHospital().getId())
                : tickets.findByCreatedByIdOrderByCreatedAtDesc(user.getId());
        return rows.stream().map(TicketView::of).toList();
    }

    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    public TicketView create(@AuthenticationPrincipal User user,@Valid @RequestBody TicketRequest request) {
        if (user.getHospital()==null) throw forbidden("Hospital membership required");
        SupportTicket ticket=new SupportTicket(); ticket.setHospital(user.getHospital()); ticket.setCreatedBy(user);
        ticket.setSubject(request.subject().trim()); ticket.setDescription(request.description().trim());
        ticket=tickets.save(ticket);
        AuditLog audit=new AuditLog(user,"CREATE_SUPPORT_TICKET","SupportTicket",ticket.getId(),"Created support request");
        audit.setHospital(user.getHospital()); audits.save(audit);
        return TicketView.of(ticket);
    }

    public record TicketRequest(@NotBlank @Size(max=180) String subject,@NotBlank @Size(max=4000) String description) {}
    public record TicketView(Long id,Long hospitalId,String hospitalName,Long createdById,String createdByName,
                             String subject,String description,String status,java.time.LocalDateTime createdAt,
                             java.time.LocalDateTime updatedAt) {
        static TicketView of(SupportTicket t){return new TicketView(t.getId(),t.getHospital().getId(),t.getHospital().getName(),
                t.getCreatedBy().getId(),t.getCreatedBy().getName(),t.getSubject(),t.getDescription(),t.getStatus().name(),t.getCreatedAt(),t.getUpdatedAt());}
    }
    static ResponseStatusException forbidden(String message){return new ResponseStatusException(HttpStatus.FORBIDDEN,message);}
}
