package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "citizens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Citizen {

    @Id
    @Column(name = "cccd", length = 12)
    private String cccd;

    @Column(name = "hoten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "ngaysinh")
    private LocalDate ngaySinh;

    @Column(name = "gioitinh", length = 10)
    private String gioiTinh;

    @Column(name = "diachithuongtru", columnDefinition = "TEXT")
    private String diaChiThuongTru;

    @Column(name = "sodienthoai", length = 15)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @Column(name = "ngaycapnhat")
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "citizen", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ZaloAccount> zaloAccounts;

    @OneToMany(mappedBy = "citizen", fetch = FetchType.LAZY)
    private List<LichHen> lichHens;

    @OneToMany(mappedBy = "citizen", fetch = FetchType.LAZY)
    private List<HoSo> hoSos;

    @OneToMany(mappedBy = "citizen", fetch = FetchType.LAZY)
    private List<GopYPhanAnh> gopYPhanAnhs;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
