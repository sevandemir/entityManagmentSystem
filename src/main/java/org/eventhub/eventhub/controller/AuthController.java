package org.eventhub.eventhub.controller;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.users.AuthResponse;
import org.eventhub.eventhub.dto.users.UserLoginRequest;
import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;
import org.eventhub.eventhub.service.impl.AuthServiceImpl;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterRequestDto request) {
        String result = authServiceImpl.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginRequest request) {
        // Artık Email mi Username mi diye kontrol etmiyoruz.
        // Servis katmanımız her ikisini de 'identifier' olarak kabul edip ID dönecek.
        String token = authServiceImpl.login(request.getIdentifier(), request.getPassword());

        System.out.println("Giriş yapıldı, ID tabanlı token üretildi.");
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/test")
    public String test(){
        return "secure endpoint çalıştı";
    }

}
