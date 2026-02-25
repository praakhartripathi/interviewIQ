package com.interviewiq.server.auth.service;

import com.interviewiq.server.auth.dto.AuthResponse;
import com.interviewiq.server.auth.dto.LoginRequest;
import com.interviewiq.server.auth.dto.RegisterRequest;
import com.interviewiq.server.user.entity.Subscription;
import com.interviewiq.server.user.entity.User;
import com.interviewiq.server.user.entity.enums.PlanType;
import com.interviewiq.server.user.entity.enums.SubscriptionStatus;
import com.interviewiq.server.user.entity.enums.UserRole;
import com.interviewiq.server.user.repository.SubscriptionRepository;
import com.interviewiq.server.user.repository.UserRepository;
import com.interviewiq.server.auth.security.JwtService;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RestTemplate restTemplate;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    public AuthService(
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(parseRole(request.getRole()));

        User saved = userRepository.save(user);

        Subscription subscription = new Subscription();
        subscription.setUser(saved);
        subscription.setPlanType(PlanType.FREE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        String token = jwtService.generateToken(saved.getEmail());
        return toAuthResponse(saved, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String token = jwtService.generateToken(user.getEmail());
        return toAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse googleLogin(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google token is required");
        }

        Map<String, Object> tokenInfo = restTemplate.getForObject(
                "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}",
                Map.class,
                idToken
        );

        if (tokenInfo == null) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        String email = asString(tokenInfo.get("email")).toLowerCase();
        String emailVerified = asString(tokenInfo.get("email_verified"));
        String audience = asString(tokenInfo.get("aud"));

        if (email.isBlank() || !"true".equalsIgnoreCase(emailVerified)) {
            throw new IllegalArgumentException("Google account email is not verified");
        }

        if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(audience)) {
            throw new IllegalArgumentException("Google token audience mismatch");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> createGoogleUser(email, tokenInfo));
        String token = jwtService.generateToken(user.getEmail());
        return toAuthResponse(user, token);
    }

    private UserRole parseRole(String rawRole) {
        if (rawRole == null) {
            return UserRole.JOB_SEEKER;
        }
        String normalized = rawRole.trim().toUpperCase().replace(' ', '_');
        return switch (normalized) {
            case "STUDENT" -> UserRole.STUDENT;
            case "ADMIN" -> UserRole.ADMIN;
            default -> UserRole.JOB_SEEKER;
        };
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    private User createGoogleUser(String email, Map<String, Object> tokenInfo) {
        User user = new User();
        String name = asString(tokenInfo.get("name"));
        if (name.isBlank()) {
            name = email.substring(0, email.indexOf('@'));
        }

        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(UserRole.JOB_SEEKER);
        User saved = userRepository.save(user);

        Subscription subscription = new Subscription();
        subscription.setUser(saved);
        subscription.setPlanType(PlanType.FREE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        return saved;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
