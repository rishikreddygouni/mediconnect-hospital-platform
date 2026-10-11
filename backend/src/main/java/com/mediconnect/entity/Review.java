package com.mediconnect.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="reviews", uniqueConstraints=@UniqueConstraint(name="uk_review_appointment",columnNames="appointment_id"))
public class Review {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hospital_id") private Hospital hospital;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="patient_id") private User patient;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="doctor_id") private User doctor;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="appointment_id") private Appointment appointment;
 @Column(nullable=false) private Integer rating;
 @Column(length=2000) private String comment;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Review(){}
 public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public User getPatient(){return patient;} public void setPatient(User v){patient=v;} public User getDoctor(){return doctor;} public void setDoctor(User v){doctor=v;} public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment v){appointment=v;} public Integer getRating(){return rating;} public void setRating(Integer v){rating=v;} public String getComment(){return comment;} public void setComment(String v){comment=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
