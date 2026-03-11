package org.eventhub.eventhub.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "blacklist:";

    // Logout olunca token'ı buraya ekle
    public void blacklist(String token, long expiryMillis) {
        redisTemplate.opsForValue().set(
                PREFIX + token,
                "revoked",
                expiryMillis,
                TimeUnit.MILLISECONDS
        );
        log.info("Token blacklist'e eklendi, TTL: {} ms", expiryMillis);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}