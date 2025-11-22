package com.acrisio.accesscontrol.domain.model;

import jakarta.persistence.Entity;
import com.acrisio.accesscontrol.domain.enums.Department;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


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