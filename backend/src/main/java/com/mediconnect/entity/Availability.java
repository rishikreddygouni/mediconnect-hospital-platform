package com.mediconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "availabilities", uniqueConstraints = @UniqueConstraint(
        name = "uk_doctor_date_time", columnNames = {"doctor_id", "date", "start_time"}))
public class Availability {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "hospital_id") private Hospital hospital;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "doctor_id") private User doctor;
    @Column(nullable = false) private LocalDate date;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;
    @Column(nullable = false) private boolean available = true;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    public Availability() {}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
    public User getDoctor(){return doctor;} public void setDoctor(User v){doctor=v;}
    public LocalDate getDate(){return date;} public void setDate(LocalDate v){date=v;}
    public LocalTime getStartTime(){return startTime;} public void setStartTime(LocalTime v){startTime=v;}
    public LocalTime getEndTime(){return endTime;} public void setEndTime(LocalTime v){endTime=v;}
    public boolean isAvailable(){return available;} public void setAvailable(boolean v){available=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
