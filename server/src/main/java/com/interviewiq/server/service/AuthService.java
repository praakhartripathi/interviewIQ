package com.interviewiq.server.service;

import com.interviewiq.server.dto.auth.AuthResponse;
import com.interviewiq.server.dto.auth.LoginRequest;
import com.interviewiq.server.dto.auth.RegisterRequest;
import com.interviewiq.server.model.Subscription;
import com.interviewiq.server.model.User;
import com.interviewiq.server.model.enums.PlanType;
import com.interviewiq.server.model.enums.SubscriptionStatus;
import com.interviewiq.server.model.enums.UserRole;
import com.interviewiq.server.repository.SubscriptionRepository;
import com.interviewiq.server.repository.UserRepository;
import com.interviewiq.server.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
}
