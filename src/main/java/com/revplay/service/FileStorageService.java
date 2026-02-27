package com.revplay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String folder) {

        try {
            Path uploadPath = Paths.get(uploadDir, folder);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", fileName);

            // Return relative path instead of full system path
            return folder + "/" + fileName;

        } catch (IOException e) {
            log.error("File storage failed", e);
            throw new RuntimeException("File storage failed", e);
        }
    }
}