package com.sift.modules.auth.controller;

import com.sift.modules.auth.dto.LoginRequest;
import com.sift.modules.auth.dto.LoginResponse;
import com.sift.modules.auth.dto.RegisterRequest;
import com.sift.modules.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest loginRequest
    ) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public LoginResponse register(
            @RequestBody RegisterRequest registerRequest
    ) {
        return authService.register(registerRequest);
    }
}