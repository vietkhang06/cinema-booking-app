package com.cinemabooking.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // Basic config
                //.httpBasic(Customizer.withDefaults())

                // Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        /*
                         * ====================================================
                         * PUBLIC APIs
                         * ====================================================
                         */
                        .requestMatchers(
                                "/api/ping",
                                "/api/v1/health",
                                "/api/v1/version",

                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                "/api/v1/movies/**",
                                "/api/v1/showtimes/**",
                                "/api/v1/cinemas/**",
                                "/api/v1/banners/**",

                                "/api/v1/seats/showtime/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/seats/lock",
                                "/api/v1/seats/release",
                                "/api/v1/seats/release-by-staff",

                                "/api/v1/bookings/**",
                                "/api/v1/payment/**",
                                "/api/v1/profile/**",
                                "/api/v1/user/**"
                        ).authenticated()

                        // Everything else
                        .anyRequest().authenticated()
                )

                // Firebase auth filter
                .addFilterBefore(
                        new FirebaseTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}