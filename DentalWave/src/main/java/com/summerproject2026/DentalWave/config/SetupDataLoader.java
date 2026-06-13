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
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.repository.OfficeRepository;

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
    private final OfficeRepository officeRepository;

    // Admin password loaded from applicationTest.properties
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
        Role managerRole = createRoleIfNotFound(Roles.ROLE_MANAGER);
        Role hrRole = createRoleIfNotFound(Roles.ROLE_HR);
        Role assistantRole = createRoleIfNotFound(Roles.ROLE_ASSISTANT);

        createUserIfNotFound("Admin", "admin",
                "admin@dentalwave.com", adminUserPassword,
                new ArrayList<>(Arrays.asList(adminRole)));

        createUserIfNotFound("Manager", managerUsername,
                "manager@dentalwave.com", managerPassword,
                new ArrayList<>(Arrays.asList(managerRole)));

        createUserIfNotFound("HR", hrUsername,
                "hr@dentalwave.com", hrPassword,
                new ArrayList<>(Arrays.asList(hrRole)));

        createUserIfNotFound("Assistant", assistantUsername,
                "assistant@dentalwave.com", assistantPassword,
                new ArrayList<>(Arrays.asList(assistantRole)));

        // ADD OFFICES HERE(that are preset!)
        createOfficeIfNotFound("Raleigh", "Wake Orthodontics Raleigh", "919-555-1111");
        createOfficeIfNotFound("Garner", "Wake Orthodontics Garner", "919-555-2222");
        createOfficeIfNotFound("Smithfield", "Wake Orthodontics Smithfield", "919-555-3333");

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
                                     String email, String password,
                                     java.util.Collection<Role> roles) {
        boolean exists = userRepository.existsByUsername(username)
                || userRepository.existsByEmail(email);

        if (!exists) {
            User user = new User();
            user.setFirstName(firstName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setRoles(roles);
            userRepository.save(user);
        }
    }

    /**
     * Helper method to set office when the application is run
     * @param name of the office
     * @param address of the office
     * @param phoneNumber of the office
     * @return office
     */
    @Transactional
    public Office createOfficeIfNotFound(String name, String address, String phoneNumber) {
        System.out.println("Checking office: " + name);

        return officeRepository.findByName(name)
                .orElseGet(() -> {
                    System.out.println("Creating office: " + name);

                    Office office = new Office();
                    office.setName(name);
                    office.setAddress(address);
                    office.setPhoneNumber(phoneNumber);
                    return officeRepository.save(office);
                });
    }
}