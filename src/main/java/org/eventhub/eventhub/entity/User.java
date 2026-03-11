package org.eventhub.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.eventhub.eventhub.enums.Role;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {  // ← implements UserDetails kaldırıldı
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private String phone;

    @Column(name = "is_active")
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public String getDisplayName() {
        return userName;
    }
}