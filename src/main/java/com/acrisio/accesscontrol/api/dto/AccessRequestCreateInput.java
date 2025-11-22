package com.acrisio.accesscontrol.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AccessRequestCreateInput(
        @NotNull @Size(min = 1, max = 3) List<Long> moduleIds,
        @NotBlank @Size(min = 20, max = 500) String justification,
        @NotNull Boolean urgent
) {}