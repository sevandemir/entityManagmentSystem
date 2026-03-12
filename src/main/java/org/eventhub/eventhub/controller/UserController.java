package org.eventhub.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventhub.eventhub.dto.users.UserResponseTokenDto;
import org.eventhub.eventhub.dto.users.UserLoginRequestDto;
import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;
import org.eventhub.eventhub.security.XssSanitizer;
import org.eventhub.eventhub.service.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserServices userService;
    private final XssSanitizer xssSanitizer; // ← eklendi

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterRequestDto request) {
        String result = userService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseTokenDto> login(@Valid @RequestBody UserLoginRequestDto request) {
        //  Log injection fix: newline karakterleri temizlendi
        log.info("Login isteği geldi: {}", xssSanitizer.sanitizeForLog(request.getIdentifier()));

        String token = userService.login(request.getIdentifier(), request.getPassword());

        log.info("Kullanıcı giriş yaptı");
        return ResponseEntity.ok(new UserResponseTokenDto(token));
    }

    @GetMapping("/hash")
    public ResponseEntity<String> hash(@RequestParam String password) {
        return ResponseEntity.ok(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode(password));
    }
}