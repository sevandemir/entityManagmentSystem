    package org.eventhub.eventhub.service.impl;

    import lombok.RequiredArgsConstructor;
    import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;
    import org.eventhub.eventhub.entity.User;
    import org.eventhub.eventhub.exception.BusinessException;
    import org.eventhub.eventhub.exception.NotFoundException;
    import org.eventhub.eventhub.mapper.UserMapper;
    import org.eventhub.eventhub.repo.UserRepository;
    import org.eventhub.eventhub.security.JwtUtil;
    import org.eventhub.eventhub.service.UserServices;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    @Service
    @RequiredArgsConstructor
    public class UserServiceImpl implements UserServices {
        private final UserRepository userRepository;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;
        private final UserMapper userMapper;
        private final JwtUtil jwtUtil;

        public String login(String identifier, String password) {
            User user = userRepository.findByUserNameOrEmail(identifier, identifier)
                    .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı!"));

            checkPassword(password, user.getPassword());
            // Token artık kullanıcının ID'sini (örn: "5") taşıyor
            return jwtUtil.createToken(user.getId(), user.getRole().name());
        }

        private void checkPassword(String rawPassword, String encodedPassword) {
            if (!bCryptPasswordEncoder.matches(rawPassword, encodedPassword)) {
                throw new BusinessException("Şifre hatalı!");
            }
        }

        @Transactional
        public String register(UserRegisterRequestDto request){
            if(userRepository.existsByEmail(request.getEmail()) || userRepository.existsByUserName(request.getUserName()))
                throw new BusinessException("Email or username already exists");

            User user = userMapper.toEntity(request);
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            return "user registered successfully";
        }
    }
