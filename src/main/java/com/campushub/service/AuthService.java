package com.campushub.service;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.LoginResponse;
import com.campushub.dto.RegisterRequest;
import com.campushub.dto.TokenResponse;
import com.campushub.dto.UserResponse;
import com.campushub.entity.User;
import com.campushub.exception.ConflictException;
import com.campushub.exception.InvalidTokenException;
import com.campushub.repository.UserRepository;
import com.campushub.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;


    public UserResponse register(RegisterRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(User.Role.STUDENT);

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                Duration.ofMillis(refreshTokenExpiry)
        );

        return new LoginResponse(accessToken, refreshToken, accessTokenExpiry);
    }

    public TokenResponse refresh(String refreshToken) {
        Claims claims = jwtUtil.parseToken(refreshToken);
        String userId = claims.getSubject();

        String stored = redisTemplate.opsForValue().get("refresh:" + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new InvalidTokenException("Refresh token is invalid or has been revoked");
        }

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return new TokenResponse(accessToken, accessTokenExpiry);
    }
}
