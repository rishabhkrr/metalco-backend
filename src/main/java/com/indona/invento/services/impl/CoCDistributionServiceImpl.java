package com.indona.invento.services.impl;

import com.indona.invento.services.CoCDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

@Service
public class CoCDistributionServiceImpl implements CoCDistributionService {

    private static final Logger logger = Logger.getLogger(CoCDistributionServiceImpl.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendCoCViaEmail(String recipientEmail, String recipientName, String cocNumber, ByteArrayOutputStream pdfStream) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("Certificate of Confidence - " + cocNumber);
            helper.setText(buildEmailBody(recipientName, cocNumber), true);

            // Attach PDF
            byte[] pdfBytes = pdfStream.toByteArray();
            helper.addAttachment("CoC_" + cocNumber + ".pdf", () -> new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");

            mailSender.send(message);
            logger.info("CoC email sent successfully to: " + recipientEmail);
        } catch (MessagingException e) {
            logger.severe("Failed to send CoC email: " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendCoCViaWhatsApp(String phoneNumber, String cocNumber, ByteArrayOutputStream pdfStream) {
        try {
            // TODO: Implement WhatsApp integration
            // This requires integration with WhatsApp Business API or a third-party service like Twilio
            // For now, we'll log a placeholder message
            logger.info("WhatsApp integration not yet implemented. Would send CoC " + cocNumber + " to: " + phoneNumber);
            
            // Example implementation with Twilio (requires twilio-java dependency):
            // Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            // Message.creator(
            //     new PhoneNumber("+1" + phoneNumber),  // To number
            //     new PhoneNumber(TWILIO_WHATSAPP_NUMBER),  // From number
            //     "Your Certificate of Confidence " + cocNumber + " is attached."
            // ).create();
        } catch (Exception e) {
            logger.severe("Failed to send CoC via WhatsApp: " + e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendCoCViaBoth(String email, String phoneNumber, String recipientName, String cocNumber, ByteArrayOutputStream pdfStream) {
        try {
            // Send via email
            sendCoCViaEmail(email, recipientName, cocNumber, pdfStream);
            
            // Send via WhatsApp
            sendCoCViaWhatsApp(phoneNumber, cocNumber, pdfStream);
            
            logger.info("CoC sent via both email and WhatsApp for: " + cocNumber);
        } catch (Exception e) {
            logger.severe("Failed to send CoC via both channels: " + e.getMessage());
            throw new RuntimeException("Failed to send CoC via both channels: " + e.getMessage(), e);
        }
    }

    private String buildEmailBody(String recipientName, String cocNumber) {
        return "<html>" +
                "<body>" +
                "<p>Dear " + recipientName + ",</p>" +
                "<p>Please find attached the Certificate of Confidence for your order.</p>" +
                "<p><strong>CoC Number:</strong> " + cocNumber + "</p>" +
                "<p>This document contains important information about your shipment and quality assurance details.</p>" +
                "<p>If you have any questions, please don't hesitate to contact us.</p>" +
                "<p>Best regards,<br/>Metalco Team</p>" +
                "</body>" +
                "</html>";
    }
}

