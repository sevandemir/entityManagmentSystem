package org.eventhub.eventhub.service.impl;


import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.exception.BusinessException;
import org.eventhub.eventhub.service.FileServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServicesImpl implements FileServices {

    // Proje ana dizininde bir uploads klasörü oluşturur
    @Value("${file.upload-dir}")
    private String uploadDir;

        public String saveImage(MultipartFile file) throws IOException {
            validateImageFile(file);
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // Dosya adının çakışmaması için başına timestamp ekliyoruz
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName; // DB'de saklanacak isim
        }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Sadece resim dosyası yüklenebilir");
        }
    }
}
