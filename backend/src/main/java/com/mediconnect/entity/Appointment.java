package com.mediconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appt_patient", columnList = "patient_id"),
        @Index(name = "idx_appt_doctor", columnList = "doctor_id"),
        @Index(name = "idx_appt_date", columnList = "appointment_date")
})
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "hospital_id") private Hospital hospital;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "patient_id") private User patient;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "doctor_id") private User doctor;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "department_id") private Department department;
    @Column(name = "appointment_date", nullable = false) private LocalDate appointmentDate;
    @Column(name = "appointment_time", nullable = false) private LocalTime appointmentTime;
    @Column(nullable = false, length = 500) private String reason;
    @Column(length = 2000) private String symptoms;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AppointmentStatus status = AppointmentStatus.PENDING;
    @Column(length = 4000) private String consultationNotes;
    @Column(length = 1000) private String cancellationReason;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    public Appointment() {}
    @PreUpdate public void touch(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
    public User getPatient(){return patient;} public void setPatient(User v){patient=v;}
    public User getDoctor(){return doctor;} public void setDoctor(User v){doctor=v;}
    public Department getDepartment(){return department;} public void setDepartment(Department v){department=v;}
    public LocalDate getAppointmentDate(){return appointmentDate;} public void setAppointmentDate(LocalDate v){appointmentDate=v;}
    public LocalTime getAppointmentTime(){return appointmentTime;} public void setAppointmentTime(LocalTime v){appointmentTime=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getSymptoms(){return symptoms;} public void setSymptoms(String v){symptoms=v;}
    public AppointmentStatus getStatus(){return status;} public void setStatus(AppointmentStatus v){status=v;}
    public String getConsultationNotes(){return consultationNotes;} public void setConsultationNotes(String v){consultationNotes=v;}
    public String getCancellationReason(){return cancellationReason;} public void setCancellationReason(String v){cancellationReason=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
