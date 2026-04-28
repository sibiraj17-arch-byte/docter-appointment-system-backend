package com.healthcare.feature.auth.service;

import com.healthcare.feature.auth.dto.*;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.exception.DuplicateResourceException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.feature.auth.repository.UserRepository;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.enums.UserRole;
import com.healthcare.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final OTPService otpService;
    private final EmailOTPService emailOTPService;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            PatientRepository patientRepository,
            OTPService otpService,
            EmailOTPService emailOTPService,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.otpService = otpService;
        this.emailOTPService = emailOTPService;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequestDTO request) {
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new DuplicateResourceException("Mobile number already registered");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User(request.getName(), request.getMobileNumber(), request.getRole());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);

        if (request.getRole() == UserRole.PATIENT && patientRepository.findByUserId(user.getId()).isEmpty()) {
            Patient patient = new Patient();
            patient.setUser(user);
            patientRepository.save(patient);
        }

        String otp = otpService.generateStoreAndReturnOTP(request.getMobileNumber());
        ensureEmailPresent(user);
        emailOTPService.sendLoginOtp(user.getEmail(), user.getName(), otp);
    }

    public void sendOTP(OTPRequestDTO request) {
        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "mobileNumber", request.getMobileNumber()));
        ensureEmailPresent(user);
        String otp = otpService.generateStoreAndReturnOTP(request.getMobileNumber());
        emailOTPService.sendLoginOtp(user.getEmail(), user.getName(), otp);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        otpService.validateOTP(request.getMobileNumber(), request.getOtp());

        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "mobileNumber", request.getMobileNumber()));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getMobileNumber(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getMobileNumber(), user.getRole().name());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setMobileNumber(user.getMobileNumber());
        response.setName(user.getName());
        response.setRole(user.getRole().name());
        return response;
    }

    public TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String incomingRefreshToken = request.getRefreshToken();
        if (!jwtUtil.validateRefreshToken(incomingRefreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String mobileNumber = jwtUtil.extractMobileNumber(incomingRefreshToken);
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User", "mobileNumber", mobileNumber));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getMobileNumber(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getMobileNumber(), user.getRole().name());

        TokenRefreshResponseDTO response = new TokenRefreshResponseDTO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        return response;
    }

    public String logout() {
        return "Logged out successfully";
    }

    private void ensureEmailPresent(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResourceNotFoundException("User email", "mobileNumber", user.getMobileNumber());
        }
    }
}
