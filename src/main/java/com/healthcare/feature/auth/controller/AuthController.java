package com.healthcare.feature.auth.controller;

import com.healthcare.feature.auth.dto.*;
import com.healthcare.feature.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Registration successful. OTP sent to the registered email address."));
    }

    @PostMapping("/login/send-otp")
    public ResponseEntity<Map<String, String>> sendOTP(@Valid @RequestBody OTPRequestDTO request) {
        authService.sendOTP(request);
        return ResponseEntity.ok(Map.of("message", "OTP sent to the email address linked with this mobile number."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        TokenRefreshResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        String message = authService.logout();
        return ResponseEntity.ok(Map.of("message", message));
    }
}
