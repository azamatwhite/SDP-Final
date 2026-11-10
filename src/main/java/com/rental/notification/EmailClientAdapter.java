package com.rental.notification;

public class EmailClientAdapter implements NotificationService {
    private final LegacyEmailClient legacyEmailClient;

    public EmailClientAdapter(LegacyEmailClient legacyEmailClient) {
        if (legacyEmailClient == null) {
            throw new IllegalArgumentException("LegacyEmailClient must not be null");
        }
        this.legacyEmailClient = legacyEmailClient;
    }

    @Override
    public void send(String to, String message) {
        legacyEmailClient.sendEmail(to, "Vehicle rental booking confirmation", message);
    }
}
