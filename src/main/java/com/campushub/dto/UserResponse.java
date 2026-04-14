package com.campushub.dto;

import com.campushub.entity.User;

public record UserResponse(Long id, String name, String email, User.Role role) {}
