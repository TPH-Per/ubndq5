package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maquay", unique = true, nullable = false, length = 10)
    private String maQuay;

    @Column(name = "tenquay", nullable = false, length = 50)
    private String tenQuay;

    @Column(name = "vitri", length = 100)
    private String viTri;

    @Column(name = "prefixso", nullable = false, length = 5)
    private String prefixSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chuyenmonid", nullable = false)
    private ChuyenMon chuyenMon;

    @Column(name = "trangthai")
    @Builder.Default
    private Boolean trangThai = true;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "quay", fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "quay", fetch = FetchType.LAZY)
    private List<LichHen> lichHens;

    @OneToMany(mappedBy = "quay", fetch = FetchType.LAZY)
    private List<HoSo> hoSos;

    @OneToMany(mappedBy = "quay", fetch = FetchType.LAZY)
    private List<GopYPhanAnh> gopYPhanAnhs;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
