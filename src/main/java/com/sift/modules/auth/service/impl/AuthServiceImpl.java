package com.sift.modules.auth.service.impl;

import com.sift.exceptions.InvalidGoogleTokenException;
import com.sift.exceptions.InvalidRefreshTokenException;
import com.sift.exceptions.UserAlreadyExistException;
import com.sift.modules.auth.dto.LoginRequest;
import com.sift.modules.auth.dto.LoginResponse;
import com.sift.modules.auth.dto.RegisterRequest;
import com.sift.modules.auth.service.AuthService;
import com.sift.modules.refresh_token.RefreshTokenService;
import com.sift.modules.user.UserEntity;
import com.sift.modules.user.UserRepository;
import com.sift.modules.user.UserResponseDTO;
import com.sift.security.GoogleIdTokenVerifierService;
import com.sift.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            GoogleIdTokenVerifierService googleIdTokenVerifierService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.googleIdTokenVerifierService = googleIdTokenVerifierService;
    }

    @Override
    public AuthResult login(LoginRequest loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.email(),
                                loginRequest.password()
                        )
                );

        UserEntity user = (UserEntity) authentication.getPrincipal();
        return issueSession(user);
    }

    @Override
    public AuthResult register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new UserAlreadyExistException();
        }

        UserEntity user = new UserEntity();
        user.setEmail(registerRequest.email());
        user.setFullName(registerRequest.fullName());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        user.setAuthProvider("local");
        userRepository.save(user);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                registerRequest.email(),
                                registerRequest.password()
                        )
                );

        UserEntity authenticatedUser = (UserEntity) authentication.getPrincipal();
        return issueSession(authenticatedUser);
    }

    @Override
    public AuthResult loginWithGoogle(String idToken) {
        GoogleIdTokenVerifierService.GooglePayload payload =
                googleIdTokenVerifierService.verify(idToken);

        if (!payload.emailVerified()) {
            throw new InvalidGoogleTokenException("Google account email is not verified");
        }

        UserEntity user = userRepository.findByEmail(payload.email())
                .map(existing -> linkGoogleId(existing, payload))
                .orElseGet(() -> createGoogleUser(payload));

        return issueSession(user);
    }

    /**
     * An existing password account signing in with Google for the first
     * time — Google has already verified this email belongs to them, so we
     * attach their Google ID rather than reject. Their password (if any)
     * keeps working exactly as before; this just adds a second way in.
     */
    private UserEntity linkGoogleId(UserEntity existing, GoogleIdTokenVerifierService.GooglePayload payload) {
        if (existing.getGoogleId() == null) {
            existing.setGoogleId(payload.googleId());
            userRepository.save(existing);
        }
        return existing;
    }

    private UserEntity createGoogleUser(GoogleIdTokenVerifierService.GooglePayload payload) {
        UserEntity user = new UserEntity();
        user.setEmail(payload.email());
        user.setFullName(payload.name() != null && !payload.name().isBlank() ? payload.name() : payload.email());
        user.setPasswordHash(null);
        user.setGoogleId(payload.googleId());
        user.setAuthProvider("google");
        return userRepository.save(user);
    }

    @Override
    public RefreshResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null) {
            throw new InvalidRefreshTokenException("No refresh token provided");
        }

        RefreshTokenService.IssuedToken rotated = refreshTokenService.rotate(rawRefreshToken);

        UserEntity user = userRepository.findById(rotated.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("User no longer exists"));

        String accessToken = jwtService.generateToken(user);
        return new RefreshResult(accessToken, rotated.rawToken());
    }

    @Override
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null) {
            refreshTokenService.revokeByToken(rawRefreshToken);
        }
    }

    private AuthResult issueSession(UserEntity user) {
        String accessToken = jwtService.generateToken(user);
        RefreshTokenService.IssuedToken issued = refreshTokenService.issue(user.getId());

        LoginResponse response = new LoginResponse(accessToken, toUserResponseDTO(user));
        return new AuthResult(response, issued.rawToken());
    }

    private UserResponseDTO toUserResponseDTO(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt(),
                user.getPassword() != null
        );
    }
}