package com.crcs.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtValidationGatewayFilterFactory extends
    AbstractGatewayFilterFactory<Object> {

  private final WebClient webClient;
  private final SecretKey secretKey;

  public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
      @Value("${auth.service.url}") String authServiceUrl,
      @Value("${jwt.secret:your-256-bit-secret-key-here-must-be-at-least-32-characters-long-for-security}") String jwtSecret) {
    this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    
    // Initialize secret key for token parsing
    byte[] keyBytes;
    try {
      keyBytes = Base64.getDecoder().decode(jwtSecret);
    } catch (IllegalArgumentException e) {
      byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
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

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> {
      String token =
          exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

      // Reject if no token or invalid format
      if(StringUtils.isBlank(token) || !StringUtils.startsWith(token, "Bearer ")) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      String jwtToken = token.substring(7);
      String userId = null;
      String userRole = null;

      // Parse and validate JWT token locally first - REQUIRED for security
      try {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload();

        Object userIdObj = claims.get("userId");
        userId = userIdObj != null ? userIdObj.toString() : null;
        userRole = claims.get("role", String.class);
      } catch (Exception e) {
        // Token parsing/validation failed - REJECT IMMEDIATELY
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      // Build exchange with X-User-Id and X-User-Role so downstream services receive them
      final ServerWebExchange finalExchange;
      if (StringUtils.isNotBlank(userId)) {
        var mutatedRequest = exchange.getRequest().mutate()
            .header("X-User-Id", userId)
            .header("X-User-Role", StringUtils.defaultString(userRole, "USER"))
            .build();
        finalExchange = exchange.mutate().request(mutatedRequest).build();
      } else {
        finalExchange = exchange;
      }

      // Additional validation with auth service to check for token revocation
      return webClient.get()
          .uri("/auth/validate")
          .header(HttpHeaders.AUTHORIZATION, token)
          .retrieve()
          .onStatus(status -> status.isError(), response ->
              Mono.error(new RuntimeException("Token validation failed")))
          .toBodilessEntity()
          .then(chain.filter(finalExchange))
          .onErrorResume(e -> {
            finalExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return finalExchange.getResponse().setComplete();
          });
    };
  }
}
