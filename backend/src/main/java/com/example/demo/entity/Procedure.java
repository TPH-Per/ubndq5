package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "procedure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "procedure_code", unique = true, nullable = false)
    private String procedureCode;

    @Column(name = "procedure_name", nullable = false)
    private String procedureName;

    @Column(name = "description")
    private String description;

    @Column(name = "processing_days", nullable = false)
    @Builder.Default
    private Integer processingDays = 15;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_schema", columnDefinition = "jsonb")
    private String formSchema;

    @Column(name = "required_documents")
    private String requiredDocuments;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_type_id")
    private ProcedureType procedureType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
