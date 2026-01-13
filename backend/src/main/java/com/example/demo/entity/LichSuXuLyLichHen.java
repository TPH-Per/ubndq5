package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lichsuxulylighhen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuXuLyLichHen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lichhenid", nullable = false)
    private LichHen lichHen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "hanhdong", nullable = false, length = 50)
    private String hanhDong;

    @Column(name = "trangthiaicu")
    private Integer trangThaiCu;

    @Column(name = "trangthaimoi")
    private Integer trangThaiMoi;

    @Column(name = "lydo", columnDefinition = "TEXT")
    private String lyDo;

    @Column(name = "thoigian")
    private LocalDateTime thoiGian;

    @PrePersist
    protected void onCreate() {
        thoiGian = LocalDateTime.now();
    }
}
