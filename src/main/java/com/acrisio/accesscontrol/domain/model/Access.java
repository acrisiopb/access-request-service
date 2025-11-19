package com.acrisio.accesscontrol.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Access implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tb_access")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tb_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tb_module", nullable = false)
    private Module module;

    @Column(name = "tb_access_granted_at", nullable = false)
    private OffsetDateTime grantedAt;

    @Column(name = "tb_access_expires_at", nullable = false)
    private OffsetDateTime expiresAt;

}
