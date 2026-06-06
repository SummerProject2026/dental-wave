package com.summerproject2026.DentalWave.config;

import com.summerproject2026.DentalWave.entity.Role;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.repository.RoleRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;

// Runs on startup to create roles and default admin user in the database
@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    // Prevents setup from running more than once
    private boolean alreadySetup = false;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Admin password loaded from application.properties
    @Value("${app.admin-user-password}")
    private String adminUserPassword;

    @Value("${app.manager-user-username}")
    private String managerUsername;

    @Value("${app.manager-user-password}")
    private String managerPassword;

    @Value("${app.hr-user-username}")
    private String hrUsername;

    @Value("${app.hr-user-password}")
    private String hrPassword;

    @Value("${app.assistant-user-username}")
    private String assistantUsername;

    @Value("${app.assistant-user-password}")
    private String assistantPassword;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;

        // Create all roles defined in the Roles enum
        Role adminRole = createRoleIfNotFound(Roles.ROLE_ADMIN);
        for (Roles.UserRoles role : Roles.UserRoles.values()) {
            createRoleIfNotFound(role.toString());
        }

        // Create default admin user
        createUserIfNotFound("Admin", "admin",
                "admin@dentalwave.com",
                new ArrayList<>(Arrays.asList(adminRole)));

        alreadySetup = true;
    }

    // Creates a role if it doesn't already exist
    @Transactional
    public Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    return roleRepository.save(role);
                });
    }

    // Creates a user if they don't already exist
    @Transactional
    public void createUserIfNotFound(String firstName, String username,
                                     String email, java.util.Collection<Role> roles) {
        boolean exists = userRepository.existsByUsername(username)
                || userRepository.existsByEmail(email);

        if (!exists) {
            User user = new User();
            user.setFirstName(firstName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(adminUserPassword));
            user.setEnabled(true);
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
}