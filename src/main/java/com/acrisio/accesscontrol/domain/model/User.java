package com.acrisio.accesscontrol.domain.model;

import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public List<Access> getActiveAccesses() {
        return accesses.stream()
                .filter(a -> a.getExpiresAt() != null &&
                        a.getExpiresAt().isAfter(java.time.OffsetDateTime.now()))
                .toList();
    }

    public Set<Module> getActiveModules() {
        return accesses.stream()
                .filter(a -> a.getExpiresAt() != null &&
                        a.getExpiresAt().isAfter(java.time.OffsetDateTime.now()))
                .map(Access::getModule)
                .collect(java.util.stream.Collectors.toSet());
    }

    // apenas solicitações ativas
    public Set<Module> getRequestedModules() {
        return requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACTIVE)
                .flatMap(r -> r.getModules().stream())
                .collect(java.util.stream.Collectors.toSet());
    }




}
