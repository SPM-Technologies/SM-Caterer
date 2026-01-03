package com.smtech.SM_Caterer.security.jwt;

import com.smtech.SM_Caterer.config.JwtProperties;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 *
 * Features:
 * - Generate access tokens with user and tenant claims
 * - Generate refresh tokens
 * - Validate token signature and expiration
 * - Extract claims from tokens
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Use a secure key for HS512
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(jwtProperties.getSecret().getBytes())
        );
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Token Provider initialized");
    }

    /**
     * Generates access token for authenticated user.
     *
     * @param authentication Spring Security Authentication object
     * @return JWT access token
     */
    public String generateAccessToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }

    /**
     * Generates access token from CustomUserDetails.
     *
     * @param userDetails User details
     * @return JWT access token
     */
    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getId());
        claims.put("username", userDetails.getUsername());
        claims.put("email", userDetails.getEmail());
        claims.put("tenantId", userDetails.getTenantId());
        claims.put("tenantCode", userDetails.getTenantCode());
        claims.put("role", userDetails.getRole().name());
        claims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userDetails.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Generates refresh token for user.
     *
     * @param userDetails User details
     * @return JWT refresh token
     */
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("type", "refresh")
                .claim("tenantId", userDetails.getTenantId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Validates JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Gets user ID from token.
     *
     * @param token JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Gets tenant ID from token.
     *
     * @param token JWT token
     * @return Tenant ID
     */
    public Long getTenantIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("tenantId", Long.class);
    }

    /**
     * Gets username from token.
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * Gets role from token.
     *
     * @param token JWT token
     * @return Role name
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Checks if token is a refresh token.
     *
     * @param token JWT token
     * @return true if refresh token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "refresh".equals(claims.get("type", String.class));
    }

    /**
     * Gets all claims from token.
     *
     * @param token JWT token
     * @return Claims
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Gets token expiration date.
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration();
    }

    /**
     * Gets access token expiration time in seconds.
     *
     * @return Expiration time in seconds
     */
    public long getExpirationInSeconds() {
        return jwtProperties.getExpiration() / 1000;
    }
}
