package com.mediconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hospitals", indexes = @Index(name = "idx_hospital_active", columnList = "active"))
public class Hospital {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 180) private String name;
    @Column(nullable = false, unique = true, length = 100) private String slug;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false, length = 30) private String subscriptionStatus = "TRIAL";
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public Hospital() {}
    public Hospital(String name, String slug) { this.name = name; this.slug = slug; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
