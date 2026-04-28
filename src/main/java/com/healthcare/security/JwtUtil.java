package com.healthcare.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // default 7 days
    private long refreshExpiration;

    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String buildToken(Long userId, String mobileNumber, String role, long ttlMillis, String tokenType) {
        return Jwts.builder()
                .subject(mobileNumber)
                .claim("userId", userId)
                .claim("mobileNumber", mobileNumber)
                .claim("role", role)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessToken(Long userId, String mobileNumber, String role) {
        return buildToken(userId, mobileNumber, role, expiration, TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(Long userId, String mobileNumber, String role) {
        return buildToken(userId, mobileNumber, role, refreshExpiration, TOKEN_TYPE_REFRESH);
    }

    

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractMobileNumber(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return expectedType.equals(tokenType) && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    /** Backward compatible alias that validates only access tokens. */
    public boolean validateToken(String token) {
        return validateAccessToken(token);
    }
}
