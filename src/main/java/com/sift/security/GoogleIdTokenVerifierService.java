package com.sift.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sift.exceptions.InvalidGoogleTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
public class GoogleIdTokenVerifierService {

    private static final String ISSUER_HTTPS = "https://accounts.google.com";
    private static final String ISSUER_BARE = "accounts.google.com";
    private static final String JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    @Value("${google.client-id}")
    private String clientId;

    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    public GoogleIdTokenVerifierService() throws MalformedURLException {
        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(JWKS_URL));
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource)
        );
        this.jwtProcessor = processor;
    }

    public record GooglePayload(
            String googleId,
            String email,
            boolean emailVerified,
            String name
    ) {}

    /**
     * Verifies the ID token's signature against Google's published keys,
     * then checks issuer, audience (must match our own client ID — this is
     * what stops a token minted for a *different* Google app being replayed
     * against this backend), and expiry before trusting any of its claims.
     */
    public GooglePayload verify(String idToken) {
        try {
            JWTClaimsSet claims = jwtProcessor.process(idToken, null);

            String issuer = claims.getIssuer();
            if (!ISSUER_HTTPS.equals(issuer) && !ISSUER_BARE.equals(issuer)) {
                throw new InvalidGoogleTokenException("Unexpected token issuer");
            }

            List<String> audience = claims.getAudience();
            if (audience == null || !audience.contains(clientId)) {
                throw new InvalidGoogleTokenException("Token was not issued for this application");
            }

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new InvalidGoogleTokenException("Token has expired");
            }

            String googleId = claims.getSubject();
            String email = claims.getStringClaim("email");
            Boolean emailVerified = claims.getBooleanClaim("email_verified");
            String name = claims.getStringClaim("name");

            return new GooglePayload(googleId, email, Boolean.TRUE.equals(emailVerified), name);

        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new InvalidGoogleTokenException("Invalid Google token");
        }
    }
}