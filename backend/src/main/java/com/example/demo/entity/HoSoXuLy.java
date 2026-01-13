package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "hosoxuly")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoSoXuLy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hosoid", nullable = false)
    private HoSo hoSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "hanhdong", nullable = false, length = 50)
    private String hanhDong;

    @Column(name = "trangthiaicu")
    private Integer trangThaiCu;

    @Column(name = "trangthaimoi")
    private Integer trangThaiMoi;

    @Column(name = "noidung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filedinhkem", columnDefinition = "jsonb")
    private List<Map<String, Object>> fileDinhKem;

    @Column(name = "thoigianbatdau")
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoigianketthuc")
    private LocalDateTime thoiGianKetThuc;

    @PrePersist
    protected void onCreate() {
        thoiGianBatDau = LocalDateTime.now();
    }
}
