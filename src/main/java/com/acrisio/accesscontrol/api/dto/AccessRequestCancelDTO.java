package com.acrisio.accesscontrol.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccessRequestCancelDTO(
        @NotNull Long id,
        @NotNull @Size(min = 10, max = 200) String reason
) {}