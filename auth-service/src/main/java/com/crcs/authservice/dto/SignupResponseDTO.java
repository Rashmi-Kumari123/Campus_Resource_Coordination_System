package com.crcs.authservice.dto;

import java.util.Map;

public class SignupResponseDTO {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private Map<String, Object> claims;
    private UserInfoDTO user;

    public SignupResponseDTO() {
    }

    public SignupResponseDTO(String token, String refreshToken, Long expiresIn, Map<String, Object> claims, UserInfoDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.claims = claims;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }

    public UserInfoDTO getUser() {
        return user;
    }

    public void setUser(UserInfoDTO user) {
        this.user = user;
    }
}
