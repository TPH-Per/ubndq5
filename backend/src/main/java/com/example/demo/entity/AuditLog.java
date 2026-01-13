package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "auditlog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;

    @Column(name = "hanhdong", nullable = false, length = 50)
    private String hanhDong;

    @Column(name = "bangdulieu", length = 50)
    private String bangDuLieu;

    @Column(name = "recordid")
    private Integer recordId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dulieucu", columnDefinition = "jsonb")
    private Map<String, Object> duLieuCu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dulieumoi", columnDefinition = "jsonb")
    private Map<String, Object> duLieuMoi;

    @Column(name = "thoigian")
    private LocalDateTime thoiGian;

    @Column(name = "ipaddress", length = 50)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        thoiGian = LocalDateTime.now();
    }
}
