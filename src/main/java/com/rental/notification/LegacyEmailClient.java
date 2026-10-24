package com.rental.notification;

public class LegacyEmailClient {
    public void sendEmail(String recipient, String subject, String body) {
        System.out.println("Simulated email to " + recipient + ": " + subject + " - " + body);
    }
}
