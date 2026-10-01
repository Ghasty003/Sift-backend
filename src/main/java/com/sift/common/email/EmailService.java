package com.sift.common.email;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}