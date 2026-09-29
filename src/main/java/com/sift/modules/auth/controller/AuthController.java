package com.sift.modules.auth.controller;

import com.sift.modules.auth.dto.LoginRequest;
import com.sift.modules.auth.dto.LoginResponse;
import com.sift.modules.auth.dto.RegisterRequest;
import com.sift.modules.auth.service.AuthService;
import com.sift.security.RefreshCookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieUtil refreshCookieUtil;

    public AuthController(AuthService authService, RefreshCookieUtil refreshCookieUtil) {
        this.authService = authService;
        this.refreshCookieUtil = refreshCookieUtil;
    }

    public record GoogleLoginRequest(@NotBlank String idToken) {}
    public record AccessTokenResponse(String accessToken) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return withRefreshCookie(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return withRefreshCookie(authService.register(registerRequest));
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return withRefreshCookie(authService.loginWithGoogle(request.idToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(HttpServletRequest request) {
        String rawRefreshToken = extractCookie(request);
        AuthService.RefreshResult result = authService.refresh(rawRefreshToken);

        ResponseCookie cookie = refreshCookieUtil.build(result.newRawRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(result.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String rawRefreshToken = extractCookie(request);
        if (rawRefreshToken != null) {
            authService.logout(rawRefreshToken);
        }

        ResponseCookie cleared = refreshCookieUtil.clear();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    private ResponseEntity<LoginResponse> withRefreshCookie(AuthService.AuthResult result) {
        ResponseCookie cookie = refreshCookieUtil.build(result.rawRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.loginResponse());
    }

    private String extractCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> RefreshCookieUtil.COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}