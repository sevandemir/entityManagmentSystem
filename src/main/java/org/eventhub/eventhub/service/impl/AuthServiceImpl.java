package org.eventhub.eventhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.repo.UserRepository;
import org.eventhub.eventhub.security.JwtUtil;
import org.eventhub.eventhub.service.AuthServices;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthServices {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new  BCryptPasswordEncoder();

    private final JwtUtil jwtUtil;

    public String login(String identifier, String password) {
        User user = userRepository.findByUserNameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Şifre hatalı!");
        }

        // Token artık kullanıcının ID'sini (örn: "5") taşıyor
        return jwtUtil.createToken(user.getId(), user.getRole().name());
    }

    private void checkPassword(String rawPassword, String encodedPassword) {
        if (!bCryptPasswordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("Şifre hatalı!");
        }
    }

    @Transactional
    public String register(UserRegisterRequestDto request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UsernameNotFoundException("Email already exists");
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return "user registered successfully";
    }
}
