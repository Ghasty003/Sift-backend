package com.sift.modules.auth.service.impl;

import com.sift.exceptions.UserAlreadyExistException;
import com.sift.modules.auth.dto.LoginRequest;
import com.sift.modules.auth.dto.LoginResponse;
import com.sift.modules.auth.dto.RegisterRequest;
import com.sift.modules.auth.service.AuthService;
import com.sift.modules.user.UserEntity;
import com.sift.modules.user.UserRepository;
import com.sift.modules.user.UserResponseDTO;
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

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.email(),
                                loginRequest.password()
                        )
                );

        UserEntity user = (UserEntity) authentication.getPrincipal();

        assert user != null;
        String token = jwtService.generateToken(user);

        return new LoginResponse(token, toUserResponseDTO(user));
    }

    @Override
    public LoginResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new UserAlreadyExistException();
        }

        UserEntity user = new UserEntity();

        user.setEmail(registerRequest.email());
        user.setFullName(registerRequest.fullName());
        user.setPasswordHash(
                passwordEncoder.encode(registerRequest.password())
        );

        userRepository.save(user);

        /*
         * Authenticate the newly-created user.
         *
         * This means registration behaves like:
         *
         * create account
         *      ↓
         * authenticate
         *      ↓
         * generate JWT
         */
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                registerRequest.email(),
                                registerRequest.password()
                        )
                );

        UserEntity authenticatedUser =
                (UserEntity) authentication.getPrincipal();

        assert authenticatedUser != null;
        String token = jwtService.generateToken(authenticatedUser);

        return new LoginResponse(token, toUserResponseDTO(authenticatedUser));
    }

    private UserResponseDTO toUserResponseDTO(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt()
        );
    }
}