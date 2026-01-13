package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chuyenmon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChuyenMon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "machuyenmon", unique = true, nullable = false, length = 20)
    private String maChuyenMon;

    @Column(name = "tenchuyenmon", nullable = false, length = 100)
    private String tenChuyenMon;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "trangthai")
    @Builder.Default
    private Boolean trangThai = true;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "chuyenMon", fetch = FetchType.LAZY)
    private List<Quay> quays;

    @OneToMany(mappedBy = "chuyenMon", fetch = FetchType.LAZY)
    private List<LoaiThuTuc> loaiThuTucs;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
