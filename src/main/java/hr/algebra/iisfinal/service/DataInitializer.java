package hr.algebra.iisfinal.service;

import hr.algebra.iisfinal.model.AppUser;
import hr.algebra.iisfinal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("FULL_ACCESS")
                    .build());
            log.info("Created admin user (FULL_ACCESS) — password: admin123");
        }
        if (userRepository.findByUsername("viewer").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .username("viewer")
                    .password(passwordEncoder.encode("viewer123"))
                    .role("READ_ONLY")
                    .build());
            log.info("Created viewer user (READ_ONLY) — password: viewer123");
        }
    }
}