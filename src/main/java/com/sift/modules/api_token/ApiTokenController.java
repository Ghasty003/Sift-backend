package com.sift.modules.api_token;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/tokens")
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    public ApiTokenController(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @PostMapping("/create")
    public CreateApiTokenResponseDTO createToken(
            Authentication authentication,
            @RequestBody CreateApiTokenRequestDTO request
    ) {
        return apiTokenService.createToken(
                authentication,
                request
        );
    }

    @GetMapping
    public ResponseEntity<List<ApiTokenResponseDTO>> getUserTokens(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                apiTokenService.getUserTokens(authentication)
        );
    }

    @PostMapping("/{tokenId}/revoke")
    public ResponseEntity<Void> revokeToken(
            Authentication authentication,
            @PathVariable String tokenId
    ) {

        apiTokenService.revokeToken(
                authentication,
                tokenId
        );

        return ResponseEntity.noContent().build();
    }
}
