package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "gopyphananhtraloi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GopYPhanAnhTraLoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gopyid", nullable = false)
    private GopYPhanAnh gopYPhanAnh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "noidung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filedinhkem", columnDefinition = "jsonb")
    private List<Map<String, Object>> fileDinhKem;

    @Column(name = "trangthiaicu")
    private Integer trangThaiCu;

    @Column(name = "trangthaimoi")
    private Integer trangThaiMoi;

    @Column(name = "ispublic")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "ngaytraloi")
    private LocalDateTime ngayTraLoi;

    @PrePersist
    protected void onCreate() {
        ngayTraLoi = LocalDateTime.now();
    }
}
