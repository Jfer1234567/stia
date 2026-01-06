package com.stia.config;

import com.stia.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- AQUÍ ESTABA EL ERROR ---
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
        // CORRECCIÓN: Pasamos 'userDetailsService' DIRECTAMENTE dentro del paréntesis
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService);

        // Ya NO usamos auth.setUserDetailsService(...) porque esa línea daba error.

        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
    // ----------------------------

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registro", "/api/auth/**", "/css/**", "/js/**", "/error").permitAll()
                        .requestMatchers("/profesor/**").hasRole("DOCENTE")
                        .requestMatchers("/padre/**").hasRole("PADRE")
                        .requestMatchers("/alumno/**").hasRole("ESTUDIANTE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}