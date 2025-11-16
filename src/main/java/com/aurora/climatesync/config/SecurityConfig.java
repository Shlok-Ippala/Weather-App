package com.aurora.climatesync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // This endpoint is for testing and visualization only. Not used in production.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/events").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}
