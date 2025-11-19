package com.acrisio.accesscontrol.api.dto;

import com.acrisio.accesscontrol.domain.enums.Department;

public record UserDTO(
        Long id,
        String name,
        String email,
        Department department
) {
}
