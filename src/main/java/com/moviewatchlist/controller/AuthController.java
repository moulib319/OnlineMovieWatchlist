package com.moviewatchlist.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviewatchlist.dto.LoginRequest;
import com.moviewatchlist.dto.RegisterRequest;
import com.moviewatchlist.entity.User;
import com.moviewatchlist.exception.ApiException;
import com.moviewatchlist.repository.UserRepository;
import com.moviewatchlist.service.CurrentUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CurrentUserService currentUserService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        String name = request.getName().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "An account with this email already exists.");
        }

        User user = new User(name, email,
                passwordEncoder.encode(request.getPassword()), "USER");
        userRepository.save(user);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Registration successful. Please log in.");
        body.put("userId", user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.getEmail().trim().toLowerCase();
        String expectedRole = request.getExpectedRole() == null
                ? "USER" : request.getExpectedRole().trim().toUpperCase();

        if (!"USER".equals(expectedRole) && !"ADMIN".equals(expectedRole)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid login type.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        String actualRole = authentication.getAuthorities().isEmpty()
                ? "" : authentication.getAuthorities().iterator().next().getAuthority()
                        .replace("ROLE_", "");

        if (!expectedRole.equals(actualRole)) {
            if ("ADMIN".equals(expectedRole)) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "This account is not an administrator account.");
            }
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Administrators must use the admin login page.");
        }

        HttpSession session = httpRequest.getSession(true);
        try {
            httpRequest.changeSessionId();
        } catch (IllegalStateException ignored) {
            // session was just created; nothing to change
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "Invalid email or password."));

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Login successful.");
        body.put("userId", user.getUserId());
        body.put("name", user.getName());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());
        body.put("sessionId", session.getId());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        User user = currentUserService.getCurrentUser();
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("userId", user.getUserId());
        body.put("name", user.getName());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Logged out successfully.");
        return ResponseEntity.ok(body);
    }
}
