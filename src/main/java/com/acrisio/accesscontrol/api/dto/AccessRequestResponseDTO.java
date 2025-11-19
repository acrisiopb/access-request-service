package com.acrisio.accesscontrol.api.dto;

import com.acrisio.accesscontrol.domain.enums.RequestStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record AccessRequestResponseDTO(
        Long id,
        String protocol,
        RequestStatus status,
        String justification,
        Boolean urgent,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        String deniedReason,
        List<ModuleDTO> modules
) {
}
