package com.sift.modules.auth.dto;

import com.sift.modules.user.UserResponseDTO;

public record LoginResponse(
        String accessToken,
        UserResponseDTO user
) {
}