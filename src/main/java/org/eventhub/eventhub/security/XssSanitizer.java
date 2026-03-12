package org.eventhub.eventhub.security;

import org.springframework.stereotype.Component;

/**
 * XSS saldırılarına karşı gelen string input'ları temizler.
 * HTML özel karakterlerini encode eder, tehlikeli pattern'ları kaldırır.
 */
@Component
public class XssSanitizer {

    /**
     * Tehlikeli HTML/JS karakterlerini encode eder.
     * Controller veya service katmanında kullanılabilir.
     */
    public String sanitize(String input) {
        if (input == null) return null;

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;")
                .replace(";", "&#x3B;");
    }

    /**
     * Log injection'a karşı: newline ve carriage return karakterlerini temizler.
     * Log satırlarına yazılacak her değer için kullanılmalı.
     */
    public String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_");
    }
}