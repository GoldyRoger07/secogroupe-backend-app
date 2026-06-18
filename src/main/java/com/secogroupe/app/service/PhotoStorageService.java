package com.secogroupe.app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhotoStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public String store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir, "photos");
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de sauvegarder le fichier photo", e);
        }
        return baseUrl + "/uploads/photos/" + filename;
    }

    public void delete(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith(baseUrl)) return;
        String relativePath = photoUrl.substring(baseUrl.length());
        // relativePath = "/uploads/photos/filename.jpg"
        Path filePath = Paths.get(uploadDir, relativePath.replace("/uploads/", ""));
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Impossible de supprimer la photo: {}", filePath);
        }
    }
}
