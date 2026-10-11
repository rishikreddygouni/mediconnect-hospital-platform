package com.mediconnect.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="notifications")
public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hospital_id") private Hospital hospital;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="user_id") private User user;
 @Column(nullable=false,length=180) private String title;
 @Column(nullable=false,length=1000) private String message;
 @Column(length=40) private String type;
 @Column(nullable=false) private boolean readFlag=false;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Notification(){} public Notification(User u,String t,String m,String ty){user=u;title=t;message=m;type=ty;}
 public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public User getUser(){return user;} public void setUser(User v){user=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;} public String getType(){return type;} public void setType(String v){type=v;} public boolean isReadFlag(){return readFlag;} public void setReadFlag(boolean v){readFlag=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
