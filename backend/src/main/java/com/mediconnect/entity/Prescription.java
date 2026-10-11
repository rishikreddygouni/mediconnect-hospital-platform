package com.mediconnect.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="prescriptions")
public class Prescription {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hospital_id") private Hospital hospital;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="patient_id") private User patient;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="doctor_id") private User doctor;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="appointment_id") private Appointment appointment;
 @Column(nullable=false,length=200) private String medicine;
 @Column(length=120) private String dosage;
 @Column(length=120) private String frequency;
 @Column(length=120) private String duration;
 @Column(length=1000) private String instructions;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Prescription(){}
 public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public User getPatient(){return patient;} public void setPatient(User v){patient=v;} public User getDoctor(){return doctor;} public void setDoctor(User v){doctor=v;} public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment v){appointment=v;} public String getMedicine(){return medicine;} public void setMedicine(String v){medicine=v;} public String getDosage(){return dosage;} public void setDosage(String v){dosage=v;} public String getFrequency(){return frequency;} public void setFrequency(String v){frequency=v;} public String getDuration(){return duration;} public void setDuration(String v){duration=v;} public String getInstructions(){return instructions;} public void setInstructions(String v){instructions=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
