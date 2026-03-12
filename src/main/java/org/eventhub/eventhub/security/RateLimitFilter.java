package org.eventhub.eventhub.security;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /*
     * Bucket yapısı:
     *  - Her IP'ye dakikada 20 istek hakkı
     *  - Greedy refill: tokenlar dakika sonunda toplu değil, sürekli yenilenir
     *    (20 token / 60 saniye = her 3 saniyede 1 token yenilenir)
     */
    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        20,
                        Refill.greedy(20, Duration.ofMinutes(1))
                ))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OPTIONS isteklerini (CORS preflight) filtreden muaf tut
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Sadece auth endpoint'lerini koru
        if (!request.getRequestURI().startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Çok fazla istek. Lütfen bekleyin.\"}");
        }
    }

    /**
     * IP tespiti — IP Spoofing korumalı.
     *
     * Eski kod X-Forwarded-For'un ilk değerini alıyordu.
     * Bu değer saldırgan tarafından manipüle edilebilir:
     *   X-Forwarded-For: 1.2.3.4 (sahte), gerçek_ip
     *
     * Güvenli kural:
     *  - Eğer arkanda güvenilir bir reverse proxy (nginx, AWS ALB) VARSA:
     *    Son IP'yi al → proxy'nin eklediği gerçek IP'dir.
     *  - Eğer proxy YOKSA (direkt internet):
     *    getRemoteAddr() kullan, header'a hiç bakma.
     *
     * Bu projede proxy varsayımıyla son IP alınıyor.
     * Proxy yoksa bu metodu sadece getRemoteAddr() döndürecek şekilde değiştir.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            // Son IP = proxy'nin güvenilir olarak eklediği gerçek istemci IP'si
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }

        // Header yoksa direkt bağlantı IP'si
        return request.getRemoteAddr();
    }
}