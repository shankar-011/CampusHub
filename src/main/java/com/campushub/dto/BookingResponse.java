package com.campushub.dto;

import java.time.OffsetDateTime;

public record BookingResponse(
    Long id,
    Long userId,
    Long eventId,
    int ticketCount,
    String status,
    OffsetDateTime createdAt
) {}
