package org.eventhub.eventhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
@Component
public class JwtUtil {

    // SABİT BİR KEY (En az 256-bit / 32 karakter olmalı)
    // Gerçek projelerde bunu application.properties içinden çekmelisin.
    private static final String SECRET_STRING = "bu-benim-cok-guclu-ve-sabit-gizli-anahtarim-2026-eventhub";
    private final Key key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    private final long validityInMilliseconds = 86400000;

    public String createToken(Long userId, String role) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Hatanın nedenini loglarda görmek için:
            System.err.println("JWT Doğrulama Hatası: " + e.getMessage());
            return false;
        }
    }
}