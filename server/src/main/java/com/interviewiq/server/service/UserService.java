package com.interviewiq.server.service;

import com.interviewiq.server.dto.user.UpdateUserRequest;
import com.interviewiq.server.dto.user.UserProfileResponse;
import com.interviewiq.server.model.User;
import com.interviewiq.server.model.enums.UserRole;
import com.interviewiq.server.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public UserService(CurrentUserService currentUserService, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    public UserProfileResponse me() {
        User user = currentUserService.currentUser();
        return toProfile(user);
    }

    public UserProfileResponse update(UpdateUserRequest request) {
        User user = currentUserService.currentUser();

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            String normalized = request.getRole().trim().toUpperCase().replace(' ', '_');
            if ("STUDENT".equals(normalized)) {
                user.setRole(UserRole.STUDENT);
            } else if ("JOB_SEEKER".equals(normalized)) {
                user.setRole(UserRole.JOB_SEEKER);
            }
        }

        User saved = userRepository.save(user);
        return toProfile(saved);
    }

    private UserProfileResponse toProfile(User user) {
        int completion = 55;
        if (user.getName() != null && !user.getName().isBlank()) {
            completion += 25;
        }
        if (user.getRole() != null) {
            completion += 20;
        }

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                Math.min(100, completion)
        );
    }
}
