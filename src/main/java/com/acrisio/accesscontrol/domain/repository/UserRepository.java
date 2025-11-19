package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
