package org.tailkeep.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tailkeep.api.model.user.Role;
import org.tailkeep.api.model.user.User;
import org.tailkeep.api.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseSeederService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEMO_MODE:false}")
    private String isDemo;

    private static final String ADMIN_NICKNAME = "Admin";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin1Admin1";
    
    private static final String DEMO_NICKNAME = "Demo";
    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "Demo1Demo1";
    
    @PostConstruct
    public void seed() {
        // Create initial user if database is empty
        if (userRepository.count() != 0) {
            return;
        }

        if ("true".equalsIgnoreCase(isDemo)) {
            userRepository.save(User.builder()
                .nickname(DEMO_NICKNAME)
                .username(DEMO_USERNAME)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(Role.USER)
                .build());
            
            log.info("Seeded demo user");
            return;
        }

        userRepository.save(User.builder()
            .nickname(ADMIN_NICKNAME)
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build());
            
        log.info("Seeded admin user");
    }
}
