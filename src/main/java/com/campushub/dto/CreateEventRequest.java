package com.campushub.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateEventRequest(
    @NotBlank String title,
    String description,
    @NotBlank String location,
    @NotNull @Future OffsetDateTime eventTime,
    @NotNull @Min(1) Integer capacity
) {}
