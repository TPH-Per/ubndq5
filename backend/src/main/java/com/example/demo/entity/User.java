package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "manhanvien", unique = true, nullable = false, length = 20)
    private String maNhanVien;

    @Column(name = "hoten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "sodienthoai", length = 15)
    private String soDienThoai;

    @Column(name = "passwordhash", nullable = false, length = 500)
    private String passwordHash;

    @Column(name = "passwordsalt", length = 500)
    private String passwordSalt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleid", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quayid")
    private Quay quay;

    @Column(name = "trangthai")
    @Builder.Default
    private Boolean trangThai = true;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoitaoid")
    private User nguoiTao;

    @Column(name = "landangnhapcuoi")
    private LocalDateTime lanDangNhapCuoi;

    // Relationships
    @OneToMany(mappedBy = "nguoiTao", fetch = FetchType.LAZY)
    private List<User> usersCreated;

    @OneToMany(mappedBy = "nhanVienXuLy", fetch = FetchType.LAZY)
    private List<LichHen> lichHensXuLy;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<LichSuXuLyLichHen> lichSuXuLyLichHens;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<HoSoXuLy> hoSoXuLys;

    @OneToMany(mappedBy = "nhanVienLienQuan", fetch = FetchType.LAZY)
    private List<GopYPhanAnh> gopYPhanAnhsLienQuan;

    @OneToMany(mappedBy = "nhanVienXuLy", fetch = FetchType.LAZY)
    private List<GopYPhanAnh> gopYPhanAnhsXuLy;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GopYPhanAnhTraLoi> gopYPhanAnhTraLois;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
