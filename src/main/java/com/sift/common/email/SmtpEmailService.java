package com.sift.common.email;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Real SMTP delivery — active only under the "prod" profile, and only once
// spring.mail.* is actually configured with real credentials in
// application-prod.yaml. Plain text for now; a proper HTML template is a
// straightforward upgrade later if you want one.
@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your Sift password");
        message.setText(
                "We received a request to reset your Sift password.\n\n" +
                        "Reset it here (valid for 30 minutes):\n" + resetLink + "\n\n" +
                        "If you didn't request this, you can safely ignore this email."
        );
        mailSender.send(message);
    }
}