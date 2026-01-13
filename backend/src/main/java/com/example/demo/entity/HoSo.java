package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "hoso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoSo {

    // Trạng thái hồ sơ
    public static final int TRANG_THAI_MOI = 0;
    public static final int TRANG_THAI_DANG_XU_LY = 1;
    public static final int TRANG_THAI_CAN_BO_SUNG = 2;
    public static final int TRANG_THAI_CHO_PHE_DUYET = 3;
    public static final int TRANG_THAI_HOAN_THANH = 4;
    public static final int TRANG_THAI_TU_CHOI = 5;

    // Độ ưu tiên
    public static final int UU_TIEN_BINH_THUONG = 0;
    public static final int UU_TIEN_UU_TIEN = 1;
    public static final int UU_TIEN_KHAN_CAP = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mahoso", unique = true, nullable = false, length = 20)
    private String maHoSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zaloid", referencedColumnName = "zaloid")
    private ZaloAccount zaloAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loaithutucid", nullable = false)
    private LoaiThuTuc loaiThuTuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quayid", nullable = false)
    private Quay quay;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thongtinhoso", columnDefinition = "jsonb")
    private Map<String, Object> thongTinHoSo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filedinhkem", columnDefinition = "jsonb")
    private List<Map<String, Object>> fileDinhKem;

    @Column(name = "trangthai", nullable = false)
    @Builder.Default
    private Integer trangThai = TRANG_THAI_MOI;

    @Column(name = "douutien")
    @Builder.Default
    private Integer doUuTien = UU_TIEN_BINH_THUONG;

    @Column(name = "ngaynop")
    private LocalDateTime ngayNop;

    @Column(name = "ngayhoanthanh")
    private LocalDateTime ngayHoanThanh;

    @Column(name = "hanxuly")
    private LocalDate hanXuLy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lichhenid")
    private LichHen lichHen;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @OneToMany(mappedBy = "hoSo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HoSoXuLy> hoSoXuLys;

    @OneToMany(mappedBy = "hoSo", fetch = FetchType.LAZY)
    private List<GopYPhanAnh> gopYPhanAnhs;

    @PrePersist
    protected void onCreate() {
        ngayNop = LocalDateTime.now();
    }
}
