package com.sift.modules.api_token;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
