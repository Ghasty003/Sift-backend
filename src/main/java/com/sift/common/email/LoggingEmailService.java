package com.sift.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

// Dev-only stand-in — no SMTP is configured for local dev, so this just logs
// the reset link where you can grab it manually instead of it silently going
// nowhere. The "prod" profile swaps this out for SmtpEmailService.
@Service
@Profile("!prod")
public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.info("[DEV EMAIL] Password reset requested for {} -> {}", toEmail, resetLink);
    }
}