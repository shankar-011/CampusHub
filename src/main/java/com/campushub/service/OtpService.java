package com.campushub.service;

import com.campushub.exception.ConflictException;
import com.campushub.exception.InvalidTokenException;
import com.campushub.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final String OTP_PREFIX = "otp:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public OtpService(RedisTemplate<String, String> redisTemplate,
                      JavaMailSender mailSender,
                      UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    public void sendOtp(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        String otp = generateOtp();
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, OTP_TTL);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("CampusHub — Your verification code");
        message.setText("Your OTP is: " + otp + "\n\nThis code expires in 10 minutes. Do not share it with anyone.");
        mailSender.send(message);
    }

    public void verifyOtp(String email, String otp) {
        String stored = redisTemplate.opsForValue().get(OTP_PREFIX + email);
        if (stored == null) {
            throw new InvalidTokenException("OTP expired or not requested for this email");
        }
        if (!stored.equals(otp)) {
            throw new InvalidTokenException("Invalid OTP");
        }
        redisTemplate.delete(OTP_PREFIX + email);
    }

    private String generateOtp() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
