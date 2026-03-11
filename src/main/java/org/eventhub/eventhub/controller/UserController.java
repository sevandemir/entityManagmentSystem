package org.eventhub.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventhub.eventhub.dto.users.UserResponseTokenDto;
import org.eventhub.eventhub.dto.users.UserLoginRequestDto;
import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;
import org.eventhub.eventhub.service.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserServices userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterRequestDto request) {
        String result = userService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseTokenDto> login(@RequestBody UserLoginRequestDto request) {
        log.info("Login isteği geldi: {}", request.getIdentifier()); // ← ekle
        String token = userService.login(request.getIdentifier(), request.getPassword());

        log.info("Kullanıcı giriş yaptı");
        return ResponseEntity.ok(new UserResponseTokenDto(token));
    }
}
