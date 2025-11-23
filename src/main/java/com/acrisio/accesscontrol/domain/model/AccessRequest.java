package com.acrisio.accesscontrol.domain.model;

import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_access_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tb_access_request")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tb_user", nullable = false)
    private  User user;

    @ManyToMany
    @JoinTable(
            name = "tb_access_request_modules",
            joinColumns = @JoinColumn(name = "id_tb_access_request"),
            inverseJoinColumns = @JoinColumn(name = "id_tb_module")
    )
    private Set<Module> modules = new HashSet<>();

    @Column(name = "tb_access_request_justification", length = 500, nullable = false)
    private String  justification;

    @Column(name = "tb_access_request_urgent", nullable = false)
    private Boolean urgent;

    // Status da solicitação
    @Enumerated(EnumType.STRING)
    @Column(name = "tb_access_request_status", nullable = false)
    private RequestStatus status;

    @Column(name = "tb_access_request_protocol", unique = true, nullable = false)
    private String protocol;

    @Column(name = "tb_access_request_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "tb_access_request_denied_reason")
    private String deniedReason;

    @Column(name = "tb_access_request_expires_at")
    private OffsetDateTime expiresAt;

    // Solicitação original (para renovações)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_origin_request")
    private AccessRequest originRequest;

    // Histórico de alterações
    @OneToMany(mappedBy = "accessRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestHistory> history;


}
