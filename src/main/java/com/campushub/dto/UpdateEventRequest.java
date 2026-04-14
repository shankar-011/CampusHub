package com.campushub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record UpdateEventRequest(
    @NotBlank String title,
    String description,
    @NotBlank String location,
    @NotNull OffsetDateTime eventTime,
    @NotNull @Min(1) Integer capacity
) {}
