package com.indona.invento.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;

@RestController
@RequestMapping("blob")
public class BlobController {

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    private static final String CONTAINER_NAME = "images";

    @PostMapping("/upload")
    public ResponseEntity<HashMap<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        HashMap<String, String> response = new HashMap<>();

        try {
            // Generate unique filename
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Create Blob Service Client
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(azureStorageConnectionString)
                    .buildClient();

            // Get or create container
            BlobContainerClient containerClient =
                    blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            containerClient.createIfNotExists();

            // Create Blob Client
            BlobClient blobClient = containerClient.getBlobClient(filename);

            // Set headers to prevent auto-download
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType())   // image/png, image/jpeg
                    .setContentDisposition("inline");

            // Upload file
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            // Return image URL
            response.put("imageUrl", blobClient.getBlobUrl());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}

