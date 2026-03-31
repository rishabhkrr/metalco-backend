package com.indona.invento.util;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.imageio.ImageIO;

@Component
public class QrCodeGenerator {

    private static final int QR_CODE_SIZE = 300;

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    /**
     * Generate QR code image and upload to Azure Blob Storage
     * Returns the blob URL
     */
    public String generateQrCodeImage(String data) {
        try {
            System.out.println("=== Starting QR Code Generation ===");
            System.out.println("QR Data: " + data);

            // Generate QR code image
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", pngOutputStream);
            byte[] qrImageBytes = pngOutputStream.toByteArray();

            System.out.println("QR Image generated. Size: " + qrImageBytes.length + " bytes");

            // Upload to Azure Blob Storage
            String filename = "qr-" + UUID.randomUUID().toString() + ".png";
            System.out.println("Filename: " + filename);
            System.out.println("Azure Connection String: " + (azureStorageConnectionString != null ? "Present" : "NULL"));

            BlobServiceClientBuilder serviceClientBuilder = new BlobServiceClientBuilder()
                    .connectionString(azureStorageConnectionString);
            BlobServiceClient blobServiceClient = serviceClientBuilder.buildClient();
            System.out.println("BlobServiceClient created");

            // Get or create container for QR codes
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("qr-codes");
            System.out.println("Container client obtained for: qr-codes");

            containerClient.createIfNotExists();
            System.out.println("Container exists or created");

            // Set container to public access so URLs are accessible
            containerClient.setAccessPolicy(PublicAccessType.BLOB, null);
            System.out.println("Container set to public access");

            // Upload QR code image
            BlobClient blobClient = containerClient.getBlobClient(filename);
            System.out.println("BlobClient created for: " + filename);

            blobClient.upload(new ByteArrayInputStream(qrImageBytes), qrImageBytes.length, true);
            System.out.println("Upload completed!");

            // Return the blob URL
            String blobUrl = blobClient.getBlobUrl();
            System.out.println("Blob URL: " + blobUrl);
            System.out.println("=== QR Code Generation Complete ===");

            return blobUrl;

        } catch (Exception e) {
            System.err.println("ERROR generating QR code: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QR code image as byte array
     */
    public byte[] generateQrCodeImageBytes(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", pngOutputStream);

            return pngOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
}

