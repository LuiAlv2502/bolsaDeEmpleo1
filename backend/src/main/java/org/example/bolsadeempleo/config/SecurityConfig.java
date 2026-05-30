package org.example.bolsadeempleo.config;

import org.example.bolsadeempleo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Rutas públicas (sin token) ─────────────────────────────
                .requestMatchers(
                    "/api/auth/**",
                    "/api/publico/**",
                    "/api/oferente/registro",
                    "/api/empresa/registro"
                ).permitAll()
                // ── SPA + recursos estáticos ───────────────────────────────
                .requestMatchers(
                    "/", "/index.html",
                    "/static/**", "/favicon.ico",
                    "/*.js", "/*.css", "/*.json", "/*.png", "/*.ico"
                ).permitAll()
                // ── Admin ──────────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // ── Empresa ───────────────────────────────────────────────
                .requestMatchers("/api/empresa/**").hasRole("EMPRESA")
                // ── CV público para empresa y oferente ────────────────────
                .requestMatchers("/api/oferente/cv/ver/**").hasAnyRole("OFERENTE", "EMPRESA")
                // ── Oferente ──────────────────────────────────────────────
                .requestMatchers("/api/oferente/**").hasRole("OFERENTE")
                // ── Todo lo demás requiere autenticación ──────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
