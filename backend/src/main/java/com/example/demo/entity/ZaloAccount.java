package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "zalo_avatar", length = 500)
    private String zaloAvatar;

    @Column(name = "oa_user_id", length = 100)
    private String oaUserId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    // 1 ZaloAccount có nhiều Appointment (quan hệ chính thay thế Citizen entity)
    @OneToMany(mappedBy = "zaloAccount", fetch = FetchType.LAZY)
    private List<Appointment> appointments;
}
