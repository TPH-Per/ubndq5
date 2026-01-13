package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "zaloaccounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZaloAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "zaloid", unique = true, nullable = false, length = 100)
    private String zaloId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", nullable = false)
    private Citizen citizen;

    @Column(name = "zaloname", length = 100)
    private String zaloName;

    @Column(name = "zaloavatar", length = 500)
    private String zaloAvatar;

    @Column(name = "ngaylienket")
    private LocalDateTime ngayLienKet;

    @Column(name = "landangnhapcuoi")
    private LocalDateTime lanDangNhapCuoi;

    @Column(name = "trangthai")
    @Builder.Default
    private Boolean trangThai = true;

    @OneToMany(mappedBy = "zaloAccount", fetch = FetchType.LAZY)
    private List<LichHen> lichHens;

    @OneToMany(mappedBy = "zaloAccount", fetch = FetchType.LAZY)
    private List<HoSo> hoSos;

    @OneToMany(mappedBy = "zaloAccount", fetch = FetchType.LAZY)
    private List<GopYPhanAnh> gopYPhanAnhs;

    @PrePersist
    protected void onCreate() {
        ngayLienKet = LocalDateTime.now();
    }
}
