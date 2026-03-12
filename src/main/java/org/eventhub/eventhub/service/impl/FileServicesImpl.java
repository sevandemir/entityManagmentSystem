package org.eventhub.eventhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventhub.eventhub.exception.BusinessException;
import org.eventhub.eventhub.service.FileServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServicesImpl implements FileServices {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // İzin verilen MIME type'lar — "image/*" değil, sadece bunlar
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    // İzin verilen uzantılar — MIME type spoofing'e karşı çift kontrol
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    // Maksimum dosya boyutu: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String saveImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // UUID ile unique dosya adı — timestamp tek başına yeterli değil
        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;

        // Path traversal koruması: resolve + normalize + startsWith kontrolü
        Path filePath = uploadPath.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadPath)) {
            // Bu durum UUID ile oluşturulan dosya adında teorik olarak oluşmaz
            // ama savunmacı programlama gereği kontrol ediyoruz
            throw new BusinessException("Geçersiz dosya yolu tespit edildi");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Dosya kaydedildi: {}", fileName);

        return fileName;
    }

    private void validateImageFile(MultipartFile file) {
        // 1. Dosya boş mu?
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Dosya boş olamaz");
        }

        // 2. Boyut kontrolü (5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Dosya boyutu 5MB'ı geçemez");
        }

        // 3. MIME type kontrolü — "image/*" değil, sadece jpeg ve png
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Sadece JPG ve PNG dosyası yüklenebilir");
        }

        // 4. Uzantı kontrolü — MIME type ile uzantı tutarlı mı?
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("Sadece .jpg, .jpeg ve .png uzantılı dosyalar kabul edilir");
        }
    }

    /**
     * Orijinal dosya adından uzantıyı güvenli şekilde çıkarır.
     * Path traversal ve null kontrolü içerir.
     *
     * "photo.jpg"        → "jpg"
     * "../../etc.jpg"    → "jpg"  (path traversal etkisiz)
     * "noextension"      → BusinessException
     */
    private String getExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("Dosya adı geçersiz");
        }

        // Sadece dosya adını al, path kısmını ignore et
        String name = Paths.get(originalFilename).getFileName().toString();

        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            throw new BusinessException("Dosya uzantısı bulunamadı");
        }

        return name.substring(dotIndex + 1);
    }
}