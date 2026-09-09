package com.sift.modules.user;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser( Authentication authentication ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt()
        );
    }
}