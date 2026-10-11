package com.mediconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="support_tickets", indexes={
        @Index(name="idx_ticket_hospital_created", columnList="hospital_id,created_at"),
        @Index(name="idx_ticket_creator", columnList="created_by_id")})
public class SupportTicket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) @JoinColumn(name="hospital_id", nullable=false) private Hospital hospital;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) @JoinColumn(name="created_by_id", nullable=false) private User createdBy;
    @Column(nullable=false, length=180) private String subject;
    @Column(nullable=false, length=4000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private SupportTicketStatus status=SupportTicketStatus.OPEN;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @Column(nullable=false) private LocalDateTime updatedAt=LocalDateTime.now();
    public SupportTicket() {}
    @PreUpdate public void touch(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
    public User getCreatedBy(){return createdBy;} public void setCreatedBy(User v){createdBy=v;}
    public String getSubject(){return subject;} public void setSubject(String v){subject=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public SupportTicketStatus getStatus(){return status;} public void setStatus(SupportTicketStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
}
