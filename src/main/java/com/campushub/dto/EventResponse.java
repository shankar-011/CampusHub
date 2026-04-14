package com.campushub.dto;

import java.time.OffsetDateTime;

public record EventResponse(
    Long id,
    String title,
    String description,
    String location,
    OffsetDateTime eventTime,
    int capacity,
    int availableTickets,
    Long organizerId,
    OffsetDateTime createdAt
) {}
