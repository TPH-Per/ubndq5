package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "lichhen", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ngayhen", "quayid", "sothutu"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichHen {

    public static final int TRANG_THAI_CHO_GOI = 0;
    public static final int TRANG_THAI_DANG_XU_LY = 1;
    public static final int TRANG_THAI_HOAN_THANH = 2;
    public static final int TRANG_THAI_KHONG_DEN = 3;
    public static final int TRANG_THAI_HUY = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "malichhen", unique = true, nullable = false, length = 20)
    private String maLichHen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zaloid", referencedColumnName = "zaloid")
    private ZaloAccount zaloAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thutucid", nullable = false)
    private LoaiThuTuc loaiThuTuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quayid", nullable = false)
    private Quay quay;

    @Column(name = "ngayhen", nullable = false)
    private LocalDate ngayHen;

    @Column(name = "sothutu", nullable = false)
    private Integer soThuTu;

    @Column(name = "prefixso", nullable = false, length = 5)
    private String prefixSo;

    @Column(name = "thoigiandukien")
    private LocalTime thoiGianDuKien;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thongtinnguoidung", columnDefinition = "jsonb")
    private Map<String, Object> thongTinNguoiDung;

    @Column(name = "isscannedcccd")
    @Builder.Default
    private Boolean isScannedCCCD = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cccddata", columnDefinition = "jsonb")
    private Map<String, Object> cccdData;

    @Column(name = "trangthai", nullable = false)
    @Builder.Default
    private Integer trangThai = TRANG_THAI_CHO_GOI;

    @Column(name = "thoigiangoiso")
    private LocalDateTime thoiGianGoiSo;

    @Column(name = "thoigianbatdauxuly")
    private LocalDateTime thoiGianBatDauXuLy;

    @Column(name = "thoigianketthuc")
    private LocalDateTime thoiGianKetThuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhanvienxulyid")
    private User nhanVienXuLy;

    @Column(name = "lydohuy", columnDefinition = "TEXT")
    private String lyDoHuy;

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "lichHen", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LichSuXuLyLichHen> lichSuXuLys;

    @OneToOne(mappedBy = "lichHen", fetch = FetchType.LAZY)
    private HoSo hoSo;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
