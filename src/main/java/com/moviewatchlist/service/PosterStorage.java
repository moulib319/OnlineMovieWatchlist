package com.moviewatchlist.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.moviewatchlist.exception.ApiException;

@Service
public class PosterStorage {

    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final Path uploadDir;

    public PosterStorage() {
        this.uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create uploads folder", e);
        }
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Poster must be an image file.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Poster image is too large. Maximum size is 5 MB.");
        }

        String extension = extensionOf(file.getOriginalFilename(), contentType);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(fileName));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save the poster image.");
        }
        return "/uploads/" + fileName;
    }

    public void delete(String posterPath) {
        if (posterPath == null || !posterPath.startsWith("/uploads/")) {
            return;
        }
        String fileName = posterPath.substring("/uploads/".length());
        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            System.err.println("Could not delete poster: " + e.getMessage());
        }
    }

    private String extensionOf(String originalName, String contentType) {
        if (originalName != null) {
            String name = originalName.replaceAll("\\s+", "_");
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot < name.length() - 1) {
                String ext = name.substring(dot).toLowerCase();
                if (ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
                    return ext;
                }
            }
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/gif".equals(contentType)) {
            return ".gif";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }
}
