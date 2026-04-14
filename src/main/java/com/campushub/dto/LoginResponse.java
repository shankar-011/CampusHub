package com.campushub.dto;

public record LoginResponse(String accessToken, String refreshToken, long expiresIn) {}
