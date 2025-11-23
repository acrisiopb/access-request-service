package com.acrisio.accesscontrol.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_protocol_sequence")
public class ProtocolSequence {

    @Id
    private String protocolDate;

    private Integer counter;
}