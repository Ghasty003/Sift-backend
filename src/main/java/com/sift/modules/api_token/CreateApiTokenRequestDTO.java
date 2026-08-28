package com.sift.modules.api_token;

public record CreateApiTokenRequestDTO(
        String type,
        String name
) {
}
