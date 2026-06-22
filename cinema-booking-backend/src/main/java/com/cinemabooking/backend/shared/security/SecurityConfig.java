package com.cinemabooking.backend.shared.security;
import com.cinemabooking.backend.features.payment.model.Payment;

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
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // Basic config
                .httpBasic(Customizer.withDefaults())

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

                                // Health
                                "/api/ping",
                                "/api/v1/health",
                                "/api/v1/version",

                                // Swagger
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                // Movies
                                "/api/v1/movies",
                                "/api/v1/movies/**",

                                // chat
                                "/api/v1/chat",
                                "/api/v1/chat/**",

                                // Banners
                                "/api/v1/banners",
                                "/api/v1/banners/**",

                                // Cinemas
                                "/api/v1/cinemas",
                                "/api/v1/cinemas/**",

                                // Showtimes
                                "/api/v1/showtimes",
                                "/api/v1/showtimes/**",

                                // Seats (TEMP DEBUG MODE)
                                "/api/v1/seats/**",

                                // Webhooks
                                "/api/webhooks/**"

                        ).permitAll()

                        /*
                         * ====================================================
                         * AUTH REQUIRED APIs
                         * ====================================================
                         */
                        .requestMatchers(

                                // Profile
                                "/api/v1/profile",
                                "/api/v1/profile/**",

                                // User
                                "/api/v1/user",
                                "/api/v1/user/**",

                                // Booking
                                "/api/v1/bookings",
                                "/api/v1/bookings/**",

                                "/api/v1/booking",
                                "/api/v1/booking/**",

                                // CineShop
                                "/api/v1/cine-shop",
                                "/api/v1/cine-shop/**",

                                // Payment
                                "/api/v1/payment",
                                "/api/v1/payment/**",

                                // Vouchers
                                "/api/v1/vouchers",
                                "/api/v1/vouchers/**"

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