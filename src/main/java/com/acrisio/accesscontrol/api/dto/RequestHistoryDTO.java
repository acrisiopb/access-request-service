package com.acrisio.accesscontrol.api.dto;

import com.acrisio.accesscontrol.domain.enums.HistoryAction;
import java.time.OffsetDateTime;

public record RequestHistoryDTO(
        HistoryAction action,
        String description,
        OffsetDateTime date
) {}