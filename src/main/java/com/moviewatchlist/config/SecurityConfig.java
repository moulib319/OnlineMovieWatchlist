package com.moviewatchlist.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/admin-login.html",
                                "/user-login.html", "/register.html",
                                "/css/**", "/js/**", "/images/**", "/uploads/**",
                                "/favicon.ico", "/error", "/api/auth/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/watchlist/**").hasRole("USER")
                        .requestMatchers("/admin-dashboard.html", "/add-movie.html",
                                "/manage-movies.html")
                        .hasRole("ADMIN")
                        .requestMatchers("/user-dashboard.html", "/my-watchlist.html")
                        .hasRole("USER")
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) -> writeError(
                                request, response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Please log in to continue."))
                        .accessDeniedHandler((request, response, ex) -> writeError(
                                request, response, HttpServletResponse.SC_FORBIDDEN,
                                "You do not have permission to do that.")));
        return http.build();
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
            int status, String message) throws IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"" + message + "\",\"status\":" + status + "}");
        } else {
            boolean adminArea = uri != null && (uri.contains("admin")
                    || uri.contains("add-movie") || uri.contains("manage-movies"));
            response.sendRedirect(adminArea ? "/admin-login.html" : "/user-login.html");
        }
    }
}
