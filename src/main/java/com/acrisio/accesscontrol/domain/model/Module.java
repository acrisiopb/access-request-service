package com.acrisio.accesscontrol.domain.model;

import java.io.Serializable;

import com.acrisio.accesscontrol.domain.enums.Department;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_module")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tb_module")
    private  Long id;

    @Column(name = "tb_module_name", nullable = false)
    private  String name;

    @Column(name = "tb_module_description")
    private  String description;

    @Column(name = "tb_module_active", nullable = false)
    private Boolean active;

    // Lista de departamentos que podem acessar esse módulo
    @ElementCollection(targetClass = Department.class)
    @CollectionTable(
            name = "tb_module_departments",
            joinColumns = @JoinColumn(name = "id_tb_module")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Set<Department> permittedDepartments = new HashSet<>();

    // Módulos incompatíveis
    @ManyToMany
    @JoinTable(
            name = "tb_module_incompatibilities",
            joinColumns = @JoinColumn(name = "id_module"),
            inverseJoinColumns = @JoinColumn(name = "id_incompatible_module")
    )
    private Set<Module> incompatibleModules = new HashSet<>();
}
