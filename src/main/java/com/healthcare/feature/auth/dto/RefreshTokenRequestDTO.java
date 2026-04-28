package com.healthcare.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDTO {

    @NotBlank
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
