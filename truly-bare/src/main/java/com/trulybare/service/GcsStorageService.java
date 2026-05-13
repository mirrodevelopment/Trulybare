package com.trulybare.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Google Cloud Storage upload service.
 * Uses Application Default Credentials (gcloud auth application-default login).
 * No JSON key required.
 *
 * File naming: <entity-name>_<5-digit-random>.<ext>
 * e.g.  botanical_face_wash_48271.jpg
 */
@Service
public class GcsStorageService {

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Value("${app.upload.max-size-mb:10}")
    private long maxSizeMb;

    @Value("${app.upload.allowed-types:image/jpeg,image/png,image/webp,image/gif}")
    private String allowedTypesRaw;

    private final Random random = new Random();

    // Lazy-initialised to pick up ADC at runtime
    private Storage storage;

    private Storage getStorage() {
        if (storage == null) {
            storage = StorageOptions.getDefaultInstance().getService();
        }
        return storage;
    }

    /**
     * Validates, uploads the file to GCS and returns its public URL.
     *
     * @param file       the multipart file to upload
     * @param folder     GCS "folder" prefix, e.g. "products" or "ritual-cards"
     * @param entityName human name used as the filename base (e.g. product name or card title)
     * @return public https URL of the uploaded object
     */
    public String uploadFile(MultipartFile file, String folder, String entityName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided.");
        }

        // ── Content-type validation ──
        String contentType = file.getContentType();
        List<String> allowed = Arrays.asList(allowedTypesRaw.split(","));
        if (contentType == null || !allowed.contains(contentType.trim())) {
            throw new IllegalArgumentException(
                    "Invalid file type: " + contentType + ". Allowed: " + allowedTypesRaw);
        }

        // ── Size validation ──
        long maxBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "File too large (" + (file.getSize() / 1024 / 1024) + " MB). Max allowed: " + maxSizeMb + " MB.");
        }

        // ── Build filename: <safe-entity-name>_<5-digit-random>.<ext> ──
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        // Sanitise entity name: lowercase, non-alphanumeric → underscore, trim edges
        String base = (entityName != null && !entityName.isBlank())
                ? entityName.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("(^_|_$)", "")
                : "file";
        if (base.isEmpty()) base = "file";

        // 5-digit random (10000–99999)
        int rand = 10000 + random.nextInt(90000);
        String objectName = folder + "/" + base + "_" + rand + extension;

        // ── Upload ──
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        try {
            getStorage().create(blobInfo, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to GCS: " + e.getMessage(), e);
        }

        // Return the public URL (bucket must have allUsers READ IAM permission)
        return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
    }

    /**
     * Convenience overload — uses "uploads" as the default folder, no entity name.
     */
    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }

    /**
     * Convenience overload — uses "uploads" folder and no entity name.
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "uploads", null);
    }

    /**
     * Deletes an object from GCS given its full public URL.
     * Silently ignores failures (object may already be deleted).
     */
    public void deleteByUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;
        try {
            String prefix = "https://storage.googleapis.com/" + bucketName + "/";
            if (publicUrl.startsWith(prefix)) {
                String objectName = publicUrl.substring(prefix.length());
                getStorage().delete(BlobId.of(bucketName, objectName));
            }
        } catch (Exception ignored) {
            // Non-fatal – log in production
        }
    }
}
