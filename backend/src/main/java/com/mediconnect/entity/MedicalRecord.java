package com.mediconnect.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="medical_records")
public class MedicalRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hospital_id") private Hospital hospital;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="patient_id") private User patient;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="doctor_id") private User doctor;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="appointment_id") private Appointment appointment;
 @Column(length=500) private String diagnosis;
 @Column(length=5000) private String notes;
 @Column(length=2000) private String vitals;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public MedicalRecord(){}
 public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public User getPatient(){return patient;} public void setPatient(User v){patient=v;} public User getDoctor(){return doctor;} public void setDoctor(User v){doctor=v;} public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment v){appointment=v;} public String getDiagnosis(){return diagnosis;} public void setDiagnosis(String v){diagnosis=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public String getVitals(){return vitals;} public void setVitals(String v){vitals=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
