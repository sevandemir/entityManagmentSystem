package org.eventhub.eventhub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Order(1)
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;

    // Bu endpoint'lerde filter'ı tamamen atla
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("İstek geldi: {} {}", request.getMethod(), request.getRequestURI());

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Token boş mu?
            if (token.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (blacklistService.isBlacklisted(token)) {
                log.warn("Blacklist'teki token kullanılmaya çalışıldı!");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token geçersiz kılınmış");
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                // ❌ Eski: sendError → /error → 403
                // ✅ Yeni: sadece devam et, SecurityContext boş kalır
                // Korumalı endpoint'lerde AuthorizationFilter zaten 401 verir
                log.warn("Geçersiz token, anonim devam ediliyor");
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtUtil.isAccessToken(token)) {
                log.warn("Refresh token ile API isteği yapılmaya çalışıldı!");
                filterChain.doFilter(request, response);
                return;
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}