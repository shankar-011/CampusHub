package com.campushub.dto;

import java.util.Map;

public record ErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors
) {}
