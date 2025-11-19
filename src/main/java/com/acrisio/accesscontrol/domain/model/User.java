package com.acrisio.accesscontrol.domain.model;

import com.acrisio.accesscontrol.domain.enums.Department;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tb_user")
    private Long id;

    @Column(name = "tb_user_name", nullable = false)
    private String name;

    @Column(name = "tb_user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "tb_user_password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tb_user_department", nullable = false)
    private Department department;

    // Relacionamentos
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Access> accesses = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AccessRequest> requests = new ArrayList<>();

}
