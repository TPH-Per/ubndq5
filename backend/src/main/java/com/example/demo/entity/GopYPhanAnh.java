package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "gopyphananh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GopYPhanAnh {

    // Loại góp ý
    public static final int LOAI_GOP_Y = 1;
    public static final int LOAI_KHIEU_NAI = 2;
    public static final int LOAI_KHEN_NGOI = 3;

    // Trạng thái
    public static final int TRANG_THAI_MOI = 0;
    public static final int TRANG_THAI_DANG_XU_LY = 1;
    public static final int TRANG_THAI_DA_XU_LY = 2;
    public static final int TRANG_THAI_DONG = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "magopy", unique = true, nullable = false, length = 20)
    private String maGopY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zaloid", referencedColumnName = "zaloid")
    private ZaloAccount zaloAccount;

    @Column(name = "loaigopy", nullable = false)
    private Integer loaiGopY;

    @Column(name = "tieude", nullable = false, length = 200)
    private String tieuDe;

    @Column(name = "noidung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filedinhkem", columnDefinition = "jsonb")
    private List<Map<String, Object>> fileDinhKem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hosoid")
    private HoSo hoSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quayid")
    private Quay quay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhanvienlienquanid")
    private User nhanVienLienQuan;

    @Column(name = "trangthai", nullable = false)
    @Builder.Default
    private Integer trangThai = TRANG_THAI_MOI;

    @Column(name = "douutien")
    @Builder.Default
    private Integer doUuTien = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhanvienxulyid")
    private User nhanVienXuLy;

    @Column(name = "ghichunoibo", columnDefinition = "TEXT")
    private String ghiChuNoiBo;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @Column(name = "ngayxuly")
    private LocalDateTime ngayXuLy;

    @Column(name = "ngayhoanthanh")
    private LocalDateTime ngayHoanThanh;

    @Column(name = "danhgia")
    private Integer danhGia;

    @Column(name = "phanhoidanhgia", columnDefinition = "TEXT")
    private String phanHoiDanhGia;

    @OneToMany(mappedBy = "gopYPhanAnh", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GopYPhanAnhTraLoi> traLois;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
