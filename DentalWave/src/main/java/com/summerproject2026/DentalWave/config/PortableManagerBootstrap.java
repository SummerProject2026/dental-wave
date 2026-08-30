package com.summerproject2026.DentalWave.config;

import com.summerproject2026.DentalWave.entity.Role;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.RoleRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/** Creates or upgrades the shared portable scheduling account. */
@Component
@Profile("portable")
@ConditionalOnProperty(name = "app.portable-bootstrap", havingValue = "true")
public class PortableManagerBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OfficeRepository officeRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String initialPassword;
    private final Path dataDirectory;

    public PortableManagerBootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OfficeRepository officeRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.portable-manager-username:user}") String username,
            @Value("${app.portable-manager-password:}") String initialPassword,
            @Value("${app.portable-data-path:./data}") String dataDirectory) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.officeRepository = officeRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.initialPassword = initialPassword;
        this.dataDirectory = Path.of(dataDirectory).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        Optional<User> configuredAccount = userRepository.findByUsername(username);
        if (configuredAccount.isEmpty() && !"manager".equalsIgnoreCase(username)) {
            Optional<User> legacyManager = userRepository.findByUsername("manager");
            if (legacyManager.isPresent()) {
                User sharedUser = legacyManager.get();
                sharedUser.setUsername(username);
                sharedUser.setFirstName("DentalWave");
                sharedUser.setLastName("User");
                userRepository.save(sharedUser);
                configuredAccount = Optional.of(sharedUser);
            }
        }

        if (configuredAccount.isEmpty()) {
            if (initialPassword == null || initialPassword.length() < 12) {
                throw new IllegalStateException(
                        "The first portable launch requires a user password of at least 12 characters.");
            }

            Role managerRole = roleRepository.findByName(Roles.ROLE_MANAGER)
                    .orElseGet(() -> roleRepository.save(new Role(null, Roles.ROLE_MANAGER)));

            User sharedUser = new User();
            sharedUser.setFirstName("DentalWave");
            sharedUser.setLastName("User");
            sharedUser.setUsername(username);
            sharedUser.setEmail("user@dentalwave.local");
            sharedUser.setPassword(passwordEncoder.encode(initialPassword));
            sharedUser.setEnabled(true);
            sharedUser.setRoles(Set.of(managerRole));
            userRepository.save(sharedUser);
        }

        createOfficeIfMissing("Raleigh");
        createOfficeIfMissing("Garner");
        createOfficeIfMissing("Smithfield");

        Files.createDirectories(dataDirectory);
        Files.writeString(
                dataDirectory.resolve("user-initialized.flag"),
                "DentalWave portable user initialized." + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private void createOfficeIfMissing(String name) {
        if (officeRepository.findByName(name).isEmpty()) {
            officeRepository.save(new Office(null, name, "", ""));
        }
    }
}
