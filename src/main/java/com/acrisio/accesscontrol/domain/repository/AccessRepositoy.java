package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AccessRepositoy extends JpaRepository<Access, Long> {
    List<Access> findByUser(User user);
}
