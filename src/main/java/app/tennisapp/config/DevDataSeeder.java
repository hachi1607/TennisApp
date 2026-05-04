package app.tennisapp.config;

import app.tennisapp.entity.Role;
import app.tennisapp.entity.User;
import app.tennisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin("admin@tennisapp.com", "password123");
    }

    private void seedAdmin(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("[DEV] Admin already exists: {}", email);
            return;
        }
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build());
        log.info("[DEV] Admin seeded: {}", email);
    }
}