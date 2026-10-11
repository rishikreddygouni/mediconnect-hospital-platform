package com.mediconnect.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="audit_logs")
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hospital_id") private Hospital hospital;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="actor_id") private User actor;
 @Column(nullable=false,length=200) private String action;
 @Column(length=80) private String targetType;
 private Long targetId;
 @Column(length=2000) private String details;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public AuditLog(){} public AuditLog(User a,String ac,String tt,Long ti,String d){actor=a;action=ac;targetType=tt;targetId=ti;details=d;}
 public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
 public Long getId(){return id;} public User getActor(){return actor;} public String getAction(){return action;} public String getTargetType(){return targetType;} public Long getTargetId(){return targetId;} public String getDetails(){return details;} public LocalDateTime getCreatedAt(){return createdAt;}
}
