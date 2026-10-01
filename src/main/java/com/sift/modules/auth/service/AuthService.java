package com.sift.modules.auth.service;

import com.sift.modules.auth.dto.LoginRequest;
import com.sift.modules.auth.dto.LoginResponse;
import com.sift.modules.auth.dto.RegisterRequest;

public interface AuthService {

    record AuthResult(LoginResponse loginResponse, String rawRefreshToken) {}
    record RefreshResult(String accessToken, String newRawRefreshToken) {}

    AuthResult login(LoginRequest loginRequest);
    AuthResult register(RegisterRequest registerRequest);
    AuthResult loginWithGoogle(String idToken);
    RefreshResult refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);
}