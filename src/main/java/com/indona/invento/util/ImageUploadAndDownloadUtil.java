package com.indona.invento.util;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class ImageUploadAndDownloadUtil {

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<HashMap<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        try {
            // Generate a unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Create BlobServiceClient
            BlobServiceClientBuilder serviceClientBuilder = new BlobServiceClientBuilder()
                    .connectionString(azureStorageConnectionString);
            BlobServiceClient blobServiceClient = serviceClientBuilder.buildClient();

            // Get or create container
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("images");
            containerClient.createIfNotExists();

            // Upload file to Azure Storage
            BlobClient blobClient = containerClient.getBlobClient(filename);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Return URL of the uploaded image
            String imageUrl = blobClient.getBlobUrl();

            HashMap<String, String> imageRes = new HashMap<String, String>();
            imageRes.put("imageUrl", imageUrl);
            return ResponseEntity.ok(imageRes);
        } catch (Exception e) {
            HashMap<String, String> err = new HashMap<String, String>();
            err.put("err", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/convert-image-to-base64")
    public Base64ImageResponse convertImageToBase64(@RequestParam("url") String imageUrl) {
        try {
            String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);

            InputStream inputStream = new URL(decodedUrl).openStream();

            byte[] imageBytes = inputStream.readAllBytes();

            String base64String = Base64.getEncoder().encodeToString(imageBytes);

            return new Base64ImageResponse("data:image/jpeg;base64," + base64String);
        } catch (IOException e) {
            e.printStackTrace();
            return new Base64ImageResponse("Error fetching or converting image.");
        }
    }

    /// Helper class for Base64 image response
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Base64ImageResponse {
        private String base64;

    }
}
