package com.mediconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "departments", uniqueConstraints = @UniqueConstraint(name = "uk_department_hospital_name", columnNames = {"hospital_id", "name"}))
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "hospital_id") private Hospital hospital;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 1000) private String description;
    @Column(length = 200) private String location;
    @Column(length = 30) private String contactNumber;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    public Department() {}
    public Department(String name, String description, String location){this.name=name;this.description=description;this.location=location;}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getContactNumber(){return contactNumber;} public void setContactNumber(String v){contactNumber=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
