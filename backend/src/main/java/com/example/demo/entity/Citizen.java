package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Citizen {

    @Id
    @Column(name = "citizen_id", length = 12)
    private String citizenId; // CCCD as Primary Key

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
