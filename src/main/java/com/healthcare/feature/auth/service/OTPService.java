package com.healthcare.feature.auth.service;

import com.healthcare.exception.InvalidOTPException;
import com.healthcare.utils.OTPGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    @Value("${otp.expiration-minutes}")
    private int expirationMinutes;

    private final Map<String, OTPData> otpStore = new ConcurrentHashMap<>();

    public String generateAndStoreOTP(String mobileNumber) {
        String otp = OTPGenerator.generateOTP();
        otpStore.put(mobileNumber, new OTPData(otp, LocalDateTime.now()));
        return otp;
    }

    public String generateStoreAndReturnOTP(String mobileNumber) {
        return generateAndStoreOTP(mobileNumber);
    }

    public void validateOTP(String mobileNumber, String otp) {
        OTPData otpData = otpStore.get(mobileNumber);
        if (otpData == null) {
            throw new InvalidOTPException("OTP not found. Please request a new OTP.");
        }
        if (otpData.createdAt.plusMinutes(expirationMinutes).isBefore(LocalDateTime.now())) {
            otpStore.remove(mobileNumber);
            throw new InvalidOTPException("OTP has expired. Please request a new OTP.");
        }
        if (!otpData.otp.equals(otp)) {
            throw new InvalidOTPException("Invalid OTP. Please try again.");
        }
        otpStore.remove(mobileNumber);
    }

    private static class OTPData {
        String otp;
        LocalDateTime createdAt;

        OTPData(String otp, LocalDateTime createdAt) {
            this.otp = otp;
            this.createdAt = createdAt;
        }
    }
}
