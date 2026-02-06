package com.we4lead.backend.controller;

import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User me(@AuthenticationPrincipal Jwt jwt){
        return userService.syncUser(jwt);
    }
}

