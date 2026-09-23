package com.codeassess.backend.config;

import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(
            AdminBootstrapProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.isEnabled()) {
            logger.info("Admin bootstrap is disabled");
            return;
        }

        validateConfiguration();

        if (userRepository.findByEmail(properties.getEmail()).isPresent()) {
            logger.info("Admin bootstrap skipped because {} already exists", properties.getEmail());
            return;
        }

        User admin = new User(
                properties.getName(),
                properties.getEmail(),
                passwordEncoder.encode(properties.getPassword()),
                Role.ADMIN
        );
        userRepository.save(admin);
        logger.info("Admin bootstrap created account {}", properties.getEmail());
    }

    private void validateConfiguration() {
        if (isBlank(properties.getName()) || isBlank(properties.getEmail()) || isBlank(properties.getPassword())) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but ADMIN_BOOTSTRAP_NAME, ADMIN_BOOTSTRAP_EMAIL, and ADMIN_BOOTSTRAP_PASSWORD are required"
            );
        }

        if (properties.getPassword().length() < 12) {
            throw new IllegalStateException("Admin bootstrap password must be at least 12 characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}