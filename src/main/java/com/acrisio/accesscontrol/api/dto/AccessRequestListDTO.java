package com.acrisio.accesscontrol.api.dto;

import com.acrisio.accesscontrol.domain.enums.RequestStatus;

import java.time.OffsetDateTime;

public record AccessRequestListDTO (
        String protocol,
        RequestStatus status,
        Boolean urgent,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        String deniedReason,
        String modules
){

}
