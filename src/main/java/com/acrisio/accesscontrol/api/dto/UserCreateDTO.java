package com.acrisio.accesscontrol.api.dto;

import com.acrisio.accesscontrol.domain.enums.Department;
import jakarta.validation.constraints.*;

public record UserCreateDTO(

        @NotBlank(message = "Name is required")
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotNull(message = "Department is required")
        Department department,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password
) {}
