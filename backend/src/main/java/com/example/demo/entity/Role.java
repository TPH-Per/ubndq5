package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "rolename", unique = true, nullable = false, length = 50)
    private String roleName;

    @Column(name = "displayname", nullable = false, length = 100)
    private String displayName;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String moTa;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<User> users;
}
