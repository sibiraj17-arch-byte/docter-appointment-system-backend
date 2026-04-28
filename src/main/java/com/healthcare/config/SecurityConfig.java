package com.healthcare.config;

import com.healthcare.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            		
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/discovery/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/api/specializations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/specializations/{id}").permitAll()
                
                // Patient endpoints
                .requestMatchers("/api/patient-profiles/**").hasRole("PATIENT")
                .requestMatchers("/api/my-appointments/**").hasRole("PATIENT")
                .requestMatchers("/api/reviews/**").hasRole("PATIENT")
                .requestMatchers("/api/billing/**").hasRole("PATIENT")
                .requestMatchers("/api/patient-history/**").hasRole("PATIENT")
                
                // Doctor endpoints
                .requestMatchers("/api/professionals/**").hasRole("DOCTOR")
                .requestMatchers("/api/availability/**").hasRole("DOCTOR")
                .requestMatchers("/api/prescriptions/**").hasAnyRole("DOCTOR", "PATIENT")
                .requestMatchers("/api/medical-reports/**").hasRole("DOCTOR")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/specializations").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/specializations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/specializations/**").hasRole("ADMIN")
                .requestMatchers("/api/bookings/**").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
