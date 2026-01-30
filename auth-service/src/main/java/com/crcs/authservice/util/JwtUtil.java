package com.crcs.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final Key secretKey;
  private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60 * 10; // 10 hours
  private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30 days

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    byte[] keyBytes;
    try {
      keyBytes = Base64.getDecoder().decode(secret);
    } catch (IllegalArgumentException e) {
      byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
      if (secretBytes.length < 32) {
        byte[] padded = new byte[32];
        System.arraycopy(secretBytes, 0, padded, 0, Math.min(secretBytes.length, 32));
        for (int i = secretBytes.length; i < 32; i++) {
          padded[i] = secretBytes[i % secretBytes.length];
        }
        keyBytes = padded;
      } else {
        keyBytes = secretBytes;
      }
    }
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String email, String role, Map<String, Object> extraClaims) {
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .addClaims(extraClaims)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken(String email, String role, Map<String, Object> extraClaims) {
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .claim("type", "refresh")
        .addClaims(extraClaims)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
        .signWith(secretKey)
        .compact();
  }

  public void validateToken(String token) {
    try {
      Jwts.parser().verifyWith((SecretKey) secretKey)
          .build()
          .parseSignedClaims(token);
    } catch (SignatureException e) {
      throw new JwtException("Invalid JWT signature");
    } catch (JwtException e) {
      throw new JwtException("Invalid JWT");
    }
  }

  public Claims extractClaims(String token) {
    return Jwts.parser().verifyWith((SecretKey) secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractEmail(String token) {
    return extractClaims(token).getSubject();
  }

  public String extractUserId(String token) {
    Claims claims = extractClaims(token);
    Object userId = claims.get("userId");
    return userId != null ? userId.toString() : null;
  }

  public String extractRole(String token) {
    Claims claims = extractClaims(token);
    return claims.get("role", String.class);
  }

  public long getAccessTokenExpiration() {
    return ACCESS_TOKEN_EXPIRATION;
  }
}
