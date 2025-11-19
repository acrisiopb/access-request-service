package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email);
}
