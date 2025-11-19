package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
}
