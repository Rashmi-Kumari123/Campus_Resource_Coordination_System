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
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Role-based authorization filter for API Gateway
 * Validates JWT token and checks if user has required role
 */
@Component
public class RoleBasedGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;
    private final SecretKey secretKey;

    public RoleBasedGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                        @Value("${auth.service.url}") String authServiceUrl,
                                        @Value("${jwt.secret:your-256-bit-secret-key-here-must-be-at-least-32-characters-long-for-security}") String jwtSecret) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
        
        // Initialize secret key
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
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (StringUtils.isBlank(token) || !StringUtils.startsWith(token, "Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String jwtToken = token.substring(7);
            
            try {
                // Parse and validate token
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(jwtToken)
                        .getPayload();

                // Extract role and user ID from token
                String userRole = claims.get("role", String.class);
                String userId = claims.get("userId", String.class);
                
                // Add user info to headers for downstream services
                if (StringUtils.isNotBlank(userId)) {
                    exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", StringUtils.defaultString(userRole, "USER"))
                            .build();
                }

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}
