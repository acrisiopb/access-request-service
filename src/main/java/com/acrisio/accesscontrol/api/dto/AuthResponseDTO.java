package com.acrisio.accesscontrol.api.dto;

import java.time.OffsetDateTime;

public record AuthResponseDTO(
        String token,
        OffsetDateTime expiresAt,
        Long userId,
        String name,
        String email,
        String department
) {}