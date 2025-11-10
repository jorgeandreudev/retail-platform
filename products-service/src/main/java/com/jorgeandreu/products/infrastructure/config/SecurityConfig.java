package com.jorgeandreu.products.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain docs(HttpSecurity http) throws Exception {
        http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**");
        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**"
        ));
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.securityMatcher("/products/**");
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> auth.anyRequest().hasAnyRole("SYSTEM", "ADMIN"));
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    UserDetailsService users(PasswordEncoder pe) {
        String systemPass = System.getenv().getOrDefault("SYSTEM_USER_PASS", "changeMe123!");
        String adminPass  = System.getenv().getOrDefault("ADMIN_USER_PASS", "changeMe123!");

        return new InMemoryUserDetailsManager(
                User.withUsername("system").password(pe.encode(systemPass)).roles("SYSTEM").build(),
                User.withUsername("admin").password(pe.encode(adminPass)).roles("ADMIN").build()
        );
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}