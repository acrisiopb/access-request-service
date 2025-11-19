package com.acrisio.accesscontrol.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AccessRequestCreateDTO(

        @NotNull(message = "Module IDs are required")
        @Size(min = 1, max = 3, message = "You must select between 1 and 3 modules")
        List<Long> moduleIds,

        @NotBlank(message = "Justification is required")
        @Size(min = 20, max = 500, message = "Justification must be between 20 and 500 characters")
        String justification,

        @NotNull(message = "Urgency flag is required")
        Boolean urgent

) {
}
