package com.interviewiq.server.user.controller;

import com.interviewiq.server.user.dto.UpdateUserRequest;
import com.interviewiq.server.user.dto.UserProfileResponse;
import com.interviewiq.server.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me() {
        return ResponseEntity.ok(userService.me());
    }

    @PutMapping("/update")
    public ResponseEntity<UserProfileResponse> update(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }
}
