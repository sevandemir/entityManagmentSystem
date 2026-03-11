package org.eventhub.eventhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    @Value("${jwt.refresh-expiration}")
    private long refreshValidityInMilliseconds;

    private Key getKey() {
        byte[] keyBytes = secretString.getBytes();
        // HS256 için minimum 32 byte (256 bit) zorunlu
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret en az 32 karakter olmalı!");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token (kısa süreli, örn: 15 dakika)
    public String createAccessToken(Long userId, String role) {
        return buildToken(userId, role, validityInMilliseconds, "access");
    }

    // Refresh Token (uzun süreli, örn: 7 gün)
    public String createRefreshToken(Long userId, String role) {
        return buildToken(userId, role, refreshValidityInMilliseconds, "refresh");
    }

    private String buildToken(Long userId, String role, long expiry, String tokenType) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);
        claims.put("type", tokenType); // access mi refresh mi olduğunu token içine göm

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getKey()) // Algoritma otomatik seçilir (HS256+)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return (String) parseClaims(token).get("role");
    }

    public String getTokenType(String token) {
        try {
            return (String) parseClaims(token).get("type");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            // Önce token boş mu kontrol et
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            Claims claims = parseClaims(token);
            if (claims.getExpiration().before(new Date())) {
                log.warn("Token süresi dolmuş");
                return false;
            }
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token süresi dolmuş: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Hatalı token formatı: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("Geçersiz imza: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Token doğrulama hatası: {}", e.getMessage());
        }
        return false;
    }

    // Access token mı geldi, refresh token mı? Bunu kontrol et
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}