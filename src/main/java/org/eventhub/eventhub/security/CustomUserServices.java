package org.eventhub.eventhub.security;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.repo.UserRepository;
import org.springframework.context.annotation.Primary;
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
        return userRepository.findById(Long.parseLong(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + identifier));

    }
}