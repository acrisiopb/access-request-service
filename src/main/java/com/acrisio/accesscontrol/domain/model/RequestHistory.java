package com.acrisio.accesscontrol.domain.model;

import com.acrisio.accesscontrol.domain.enums.HistoryAction;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_request_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tb_request_history")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tb_access_request", nullable = false)
    private AccessRequest accessRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "tb_history_action", nullable = false)
    private HistoryAction action;

    @Column(name = "tb_history_description")
    private String description;

    // Data e hora da ação
    @Column(name = "tb_history_timestamp", nullable = false)
    private OffsetDateTime date;
}
