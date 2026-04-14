package com.campushub.controller;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.LoginResponse;
import com.campushub.dto.RegisterRequest;
import com.campushub.dto.SendOtpRequest;
import com.campushub.dto.TokenResponse;
import com.campushub.dto.UserResponse;
import com.campushub.service.AuthService;
import com.campushub.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Register, login and refresh tokens")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Send OTP to email",
        description = "Step 1 of registration. Sends a 6-digit OTP to the given email. OTP expires in 10 minutes."
    )
    public Map<String, String> sendOtp(@Valid @RequestBody SendOtpRequest req) {
        otpService.sendOtp(req.email());
        return Map.of("message", "OTP sent to " + req.email());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register a new user",
        description = "Step 2 of registration. Include the OTP received by email. All users register as STUDENT."
    )
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Returns access token (15 min) and refresh token (7 days)")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchange a valid refresh token for a new access token")
    public TokenResponse refresh(@RequestBody Map<String, String> body) {
        return authService.refresh(body.get("refreshToken"));
    }
}
