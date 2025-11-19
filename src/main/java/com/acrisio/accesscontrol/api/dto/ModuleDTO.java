package com.acrisio.accesscontrol.api.dto;

import java.util.Set;

public record ModuleDTO (
        Long id,
        String name,
        String description,
        Boolean active,
        Set<String> permittedDepartments,
        Set<String> incompatibleModules
) {
}
