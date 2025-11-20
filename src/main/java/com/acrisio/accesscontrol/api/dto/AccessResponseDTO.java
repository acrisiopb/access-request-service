package com.acrisio.accesscontrol.api.dto;

import java.time.OffsetDateTime;

public record AccessResponseDTO(
        Long id,
        Long userId,
        Long moduleId,
        String moduleName,
        OffsetDateTime grantedAt,
        OffsetDateTime expiresAt
) {
}
