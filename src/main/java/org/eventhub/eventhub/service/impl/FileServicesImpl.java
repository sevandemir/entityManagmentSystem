package org.eventhub.eventhub.service.impl;


import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.service.FileServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional
public class FileServicesImpl implements FileServices {

    // Proje ana dizininde bir uploads klasörü oluşturur
        private final String uploadDir = "uploads/";

        public String saveImage(MultipartFile file) throws IOException {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Dosya adının çakışmaması için başına timestamp ekliyoruz
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName; // DB'de saklanacak isim
        }

}
