package com.rental.adapter;

import com.rental.notification.EmailClientAdapter;
import com.rental.notification.LegacyEmailClient;
import com.rental.payment.LegacyPaymentAdapter;
import com.rental.payment.LegacyPaymentProcessor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdapterTest {
    @Test
    void paymentAdapterConvertsDollarsToCentsAndSetsCurrency() {
        RecordingPaymentProcessor processor = new RecordingPaymentProcessor();
        LegacyPaymentAdapter adapter = new LegacyPaymentAdapter(processor);

        adapter.charge("token-123", new BigDecimal("12.34"));

        assertEquals("token-123", processor.cardToken);
        assertEquals(1234L, processor.cents);
        assertEquals("USD", processor.currency);
    }

    @Test
    void emailAdapterForwardsRecipientAndMessage() {
        RecordingEmailClient client = new RecordingEmailClient();
        EmailClientAdapter adapter = new EmailClientAdapter(client);

        adapter.send("renter@example.com", "Your reservation is confirmed");

        assertEquals("renter@example.com", client.recipient);
        assertEquals("Vehicle rental booking confirmation", client.subject);
        assertEquals("Your reservation is confirmed", client.body);
    }

    private static class RecordingPaymentProcessor extends LegacyPaymentProcessor {
        private String cardToken;
        private long cents;
        private String currency;

        @Override
        public boolean makePayment(String cardToken, long cents, String currency) {
            this.cardToken = cardToken;
            this.cents = cents;
            this.currency = currency;
            return true;
        }
    }

    private static class RecordingEmailClient extends LegacyEmailClient {
        private String recipient;
        private String subject;
        private String body;

        @Override
        public void sendEmail(String recipient, String subject, String body) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
        }
    }
}
