package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.Access;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRepositoy extends JpaRepository<Access, Long> {
}
