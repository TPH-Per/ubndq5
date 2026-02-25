package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "\"GopYPhanAnh\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    // Type constants (matching DB: 1=Góp ý, 2=Khiếu nại, 3=Khen ngợi)
    public static final int TYPE_SUGGESTION = 1; // Góp ý
    public static final int TYPE_COMPLAINT = 2; // Khiếu nại
    public static final int TYPE_COMPLIMENT = 3; // Khen ngợi

    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_RESOLVED = 2;
    public static final int STATUS_COMPLETED = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "magopy", unique = true, nullable = false)
    private String feedbackCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", referencedColumnName = "citizen_id")
    private Citizen citizen;

    @Column(name = "zaloid")
    private String zaloId;

    @Column(name = "loaigopy", nullable = false)
    private Integer type;

    @Column(name = "tieude", nullable = false)
    private String title;

    @Column(name = "noidung", columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filedinhkem", columnDefinition = "JSONB")
    private Map<String, Object> attachments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hosoid")
    private Application application;

    @Column(name = "trangthai", nullable = false)
    @Builder.Default
    private Integer status = STATUS_PENDING;

    @Column(name = "douutien")
    @Builder.Default
    private Integer priority = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhanvienxulyid")
    private Staff respondedBy;

    @Column(name = "ghichunoibo", columnDefinition = "TEXT")
    private String internalNote;

    @Column(name = "ngaytao")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ngayxuly")
    private LocalDateTime processedAt;

    @Column(name = "ngayhoanthanh")
    private LocalDateTime completedAt;

    @Column(name = "danhgia")
    private Integer rating;

    @Column(name = "phanhoidanhgia", columnDefinition = "TEXT")
    private String ratingFeedback;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (feedbackCode == null) {
            feedbackCode = "GY-" + System.currentTimeMillis();
        }
    }
}
