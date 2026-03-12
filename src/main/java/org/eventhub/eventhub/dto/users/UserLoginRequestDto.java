package org.eventhub.eventhub.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @NotBlank(message = "Kullanıcı adı veya email boş olamaz")
    @Size(max = 100, message = "Kullanıcı adı veya email en fazla 100 karakter olabilir")
    @Pattern(
            regexp = "^[^<>\"';/\\\\]*$",
            message = "Geçersiz karakter içeriyor"
    )
    private String identifier;

    /**
     * Şifre validasyonu: içerik kontrolü yapılmaz (hash'lenecek),
     * sadece boyut sınırı uygulanır — DoS'a karşı.
     */
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, max = 100, message = "Şifre 6-100 karakter arasında olmalı")
    private String password;
}
