package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hethongcauhinh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeThongCauHinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "configkey", unique = true, nullable = false, length = 50)
    private String configKey;

    @Column(name = "configvalue", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ngaycapnhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
