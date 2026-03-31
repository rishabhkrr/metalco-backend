package com.indona.invento.services;

import java.io.ByteArrayOutputStream;

public interface CoCDistributionService {

    /**
     * Send CoC PDF via email
     */
    void sendCoCViaEmail(String recipientEmail, String recipientName, String cocNumber, ByteArrayOutputStream pdfStream);

    /**
     * Send CoC PDF via WhatsApp (requires WhatsApp API integration)
     */
    void sendCoCViaWhatsApp(String phoneNumber, String cocNumber, ByteArrayOutputStream pdfStream);

    /**
     * Send CoC PDF via both email and WhatsApp
     */
    void sendCoCViaBoth(String email, String phoneNumber, String recipientName, String cocNumber, ByteArrayOutputStream pdfStream);
}

