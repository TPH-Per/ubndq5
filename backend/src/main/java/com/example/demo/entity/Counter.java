package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "counter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "counter_code", unique = true, nullable = false)
    private String counterCode;

    @Column(name = "counter_name", nullable = false)
    private String counterName;

    @Column(name = "location")
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_type_id")
    private ProcedureType procedureType;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
