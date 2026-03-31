package com.indona.invento.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

    @Value("${server.app.url:http://localhost:8089}")
    private String serverUrl;

    private static final String CONTAINER_NAME = "images";
    private static final String LOCAL_UPLOAD_DIR = "uploads";

    /**
     * Check if Azure Storage is properly configured
     */
    private boolean isAzureConfigured() {
        return azureStorageConnectionString != null
                && !azureStorageConnectionString.contains("YOUR_ACCOUNT_NAME")
                && !azureStorageConnectionString.contains("YOUR_ACCOUNT_KEY")
                && !azureStorageConnectionString.isEmpty();
    }

    @PostMapping("/upload")
    public ResponseEntity<HashMap<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        HashMap<String, String> response = new HashMap<>();

        try {
            if (isAzureConfigured()) {
                // Azure Blob Storage upload
                return uploadToAzure(file, response);
            } else {
                // Local file storage fallback
                return uploadLocally(file, response);
            }
        } catch (IOException e) {
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Upload file to Azure Blob Storage
     */
    private ResponseEntity<HashMap<String, String>> uploadToAzure(
            MultipartFile file, HashMap<String, String> response) throws IOException {

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(azureStorageConnectionString)
                .buildClient();

        BlobContainerClient containerClient =
                blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
        containerClient.createIfNotExists();

        BlobClient blobClient = containerClient.getBlobClient(filename);

        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(file.getContentType())
                .setContentDisposition("inline");

        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);

        response.put("imageUrl", blobClient.getBlobUrl());
        return ResponseEntity.ok(response);
    }

    /**
     * Upload file to local storage as fallback when Azure is not configured
     */
    private ResponseEntity<HashMap<String, String>> uploadLocally(
            MultipartFile file, HashMap<String, String> response) throws IOException {

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(LOCAL_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file";
        String filename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        // Save file locally
        Files.copy(file.getInputStream(), filePath);

        // Return a URL that can be used to access the file
        String fileUrl = serverUrl + "/api/metalco/blob/local/" + filename;
        response.put("imageUrl", fileUrl);

        System.out.println("File uploaded locally: " + filePath.toAbsolutePath());
        return ResponseEntity.ok(response);
    }

    /**
     * Serve locally uploaded files
     */
    @GetMapping("/local/{filename}")
    public ResponseEntity<Resource> serveLocalFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(LOCAL_UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
