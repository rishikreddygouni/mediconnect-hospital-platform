package com.mediconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity @Table(name="appointment_reschedules", indexes={
        @Index(name="idx_reschedule_appt", columnList="appointment_id,created_at"),
        @Index(name="idx_reschedule_hospital", columnList="hospital_id")})
public class AppointmentReschedule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="hospital_id",nullable=false) private Hospital hospital;
    @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="appointment_id",nullable=false) private Appointment appointment;
    @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="actor_id",nullable=false) private User actor;
    @Column(name="old_date",nullable=false) private LocalDate oldDate;
    @Column(name="old_time",nullable=false) private LocalTime oldTime;
    @Column(name="new_date",nullable=false) private LocalDate newDate;
    @Column(name="new_time",nullable=false) private LocalTime newTime;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    public Long getId(){return id;} public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
    public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment v){appointment=v;}
    public User getActor(){return actor;} public void setActor(User v){actor=v;}
    public LocalDate getOldDate(){return oldDate;} public void setOldDate(LocalDate v){oldDate=v;}
    public LocalTime getOldTime(){return oldTime;} public void setOldTime(LocalTime v){oldTime=v;}
    public LocalDate getNewDate(){return newDate;} public void setNewDate(LocalDate v){newDate=v;}
    public LocalTime getNewTime(){return newTime;} public void setNewTime(LocalTime v){newTime=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
