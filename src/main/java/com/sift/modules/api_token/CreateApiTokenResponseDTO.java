package com.sift.modules.api_token;

import java.util.UUID;

public record CreateApiTokenResponseDTO(UUID id,
                                        String token,
                                        String type,
                                        String name) {
}
