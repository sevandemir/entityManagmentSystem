package org.eventhub.eventhub.security;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.repo.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
public class CustomUserServices implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user;

        // Eğer identifier sadece rakamlardan oluşuyorsa, ID ile aramayı dene
        if (identifier.matches("\\d+")) {
            user = userRepository.findById(Long.parseLong(identifier))
                    .orElseGet(() -> userRepository.findByUserNameOrEmail(identifier, identifier)
                            .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + identifier)));
        } else {
            // Rakam değilse direkt UserName veya Email ile ara
            user = userRepository.findByUserNameOrEmail(identifier, identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + identifier));
        }

        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()), // Kimlik artık String ID ("1")
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}