package com.acrisio.accesscontrol.api.dto;

import com.acrisio.accesscontrol.domain.enums.RequestStatus;

import java.time.LocalDate;

public record AccessRequestFilterDTO(
        String search,
        RequestStatus status,
        Boolean urgent,
        LocalDate startDate,
        LocalDate endDate
) {
}
