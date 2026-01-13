package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "loaithutuc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiThuTuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mathutuc", unique = true, nullable = false, length = 20)
    private String maThuTuc;

    @Column(name = "tenthutuc", nullable = false, length = 200)
    private String tenThuTuc;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String moTa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chuyenmonid", nullable = false)
    private ChuyenMon chuyenMon;

    @Column(name = "thoigianxuly", nullable = false)
    @Builder.Default
    private Integer thoiGianXuLy = 15;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "formschema", columnDefinition = "jsonb")
    private Map<String, Object> formSchema;

    @Column(name = "giaytoyeucau", columnDefinition = "TEXT")
    private String giayToYeuCau;

    @Column(name = "trangthai")
    @Builder.Default
    private Boolean trangThai = true;

    @Column(name = "thutu")
    @Builder.Default
    private Integer thuTu = 0;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "loaiThuTuc", fetch = FetchType.LAZY)
    private List<LichHen> lichHens;

    @OneToMany(mappedBy = "loaiThuTuc", fetch = FetchType.LAZY)
    private List<HoSo> hoSos;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
