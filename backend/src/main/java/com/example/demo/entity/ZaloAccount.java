package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zaloaccount")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZaloAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "zalo_id", unique = true, nullable = false, length = 100)
    private String zaloId;

    @Column(name = "zalo_name", length = 100)
    private String zaloName;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
