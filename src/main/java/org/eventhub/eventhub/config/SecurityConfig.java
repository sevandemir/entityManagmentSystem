package org.eventhub.eventhub.config;

import org.eventhub.eventhub.security.JwtFilter;
import org.eventhub.eventhub.security.JwtUtil;
import org.eventhub.eventhub.security.RateLimitFilter;
import org.eventhub.eventhub.security.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize aktif
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;

    @Value("${cors.allowed-origins}")
    private String allowedOriginsRaw;

    public SecurityConfig(JwtUtil jwtUtil, TokenBlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil, blacklistService);
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                /*
                 * CSRF KARARI:
                 * Bu proje stateless JWT kullanıyor (cookie yok, localStorage/header tabanlı).
                 * Thymeleaf form'ları olsa bile JWT header ile çalıştığı için CSRF riski düşük.
                 *
                 * CSRF AÇIK bırakılması gereken durum:
                 *   - Session tabanlı auth kullanılıyorsa
                 *   - Token cookie'de saklanıyorsa (HttpOnly cookie)
                 *
                 * Şu an: JWT Authorization header ile gönderildiği için CSRF disable güvenli.
                 * İleride cookie tabanlı auth'a geçilirse burası değiştirilmeli!
                 */
                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // JWT stateless — session oluşturma
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(ct -> {})
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data: blob:; " +
                                                "connect-src 'self'"
                                )
                        )
                        .referrerPolicy(ref -> ref
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoint'leri herkese açık
                        .requestMatchers("/api/auth/**").permitAll()

                        // Etkinlik listeleme ve detay herkese açık
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()

                        // Statik dosyalar
                        .requestMatchers("/images/**", "/uploads/**").permitAll()

                        // Admin endpoint'leri sadece ADMIN rolüne
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Diğer her şey login gerektiriyor
                        .anyRequest().authenticated()
                )

                .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.asList(allowedOriginsRaw.split(","));

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(origins);
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}