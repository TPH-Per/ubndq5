package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ProcedureType - Loại thủ tục
 * Dùng để nhóm các Procedure và Counter theo loại
 * VD: Hộ tịch, Đất đai, Kinh doanh...
 */
@Entity
@Table(name = "procedure_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
