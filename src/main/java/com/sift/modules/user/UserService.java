package com.sift.modules.user;

import com.sift.exceptions.InvalidCredentialsException;
import com.sift.modules.user.dto.ChangePasswordRequest;
import com.sift.modules.user.dto.UpdateProfileRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        return toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(Authentication authentication, UpdateProfileRequest request) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        user.setFullName(request.fullName());
        userRepository.save(user);

        return toUserResponseDTO(user);
    }

    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        userRepository.delete(user);
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