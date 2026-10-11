package com.mediconnect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role")
})
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, unique = true, length = 190) private String email;
    @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role = Role.PATIENT;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "hospital_id") private Hospital hospital;
    @Column(length = 30) private String phone;
    private LocalDate dateOfBirth;
    @Column(length = 20) private String gender;
    @Column(length = 500) private String address;
    @Column(length = 20) private String bloodGroup;
    @Column(length = 2000) private String allergies;
    @Column(length = 100) private String emergencyContact;
    @Column(length = 120) private String specialization;
    @Column(length = 200) private String qualification;
    private Integer experience;
    @Column(precision = 10, scale = 2) private BigDecimal consultationFee;
    @Column(length = 100) private String licenseNumber;
    @Column(nullable = false) private boolean approved;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}
    public User(String name, String email, String password, Role role) {
        this.name = name; this.email = email; this.password = password; this.role = role;
        this.approved = role != Role.DOCTOR;
    }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public Hospital getHospital(){return hospital;} public void setHospital(Hospital v){hospital=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate v){dateOfBirth=v;}
    public String getGender(){return gender;} public void setGender(String v){gender=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public String getBloodGroup(){return bloodGroup;} public void setBloodGroup(String v){bloodGroup=v;}
    public String getAllergies(){return allergies;} public void setAllergies(String v){allergies=v;}
    public String getEmergencyContact(){return emergencyContact;} public void setEmergencyContact(String v){emergencyContact=v;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String v){specialization=v;}
    public String getQualification(){return qualification;} public void setQualification(String v){qualification=v;}
    public Integer getExperience(){return experience;} public void setExperience(Integer v){experience=v;}
    public BigDecimal getConsultationFee(){return consultationFee;} public void setConsultationFee(BigDecimal v){consultationFee=v;}
    public String getLicenseNumber(){return licenseNumber;} public void setLicenseNumber(String v){licenseNumber=v;}
    public boolean isApproved(){return approved;} public void setApproved(boolean v){approved=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
