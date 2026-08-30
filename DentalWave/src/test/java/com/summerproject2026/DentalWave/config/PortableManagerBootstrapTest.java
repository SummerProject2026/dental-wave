package com.summerproject2026.DentalWave.config;

import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.Role;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.RoleRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortableManagerBootstrapTest {

    @TempDir
    Path dataDirectory;

    @Test
    void createsSharedUserOfficesAndInitializationFlagOnFirstRun() throws Exception {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        OfficeRepository offices = mock(OfficeRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(users.findByUsername("user")).thenReturn(Optional.empty());
        when(users.findByUsername("manager")).thenReturn(Optional.empty());
        when(roles.findByName(Roles.ROLE_MANAGER))
                .thenReturn(Optional.of(new Role(1L, Roles.ROLE_MANAGER)));
        when(passwordEncoder.encode("A strong portable password"))
                .thenReturn("bcrypt-hash");
        when(offices.findByName(any())).thenReturn(Optional.empty());

        bootstrap(users, roles, offices, passwordEncoder, "A strong portable password")
                .run(mock(ApplicationArguments.class));

        verify(users).save(argThat(user ->
                "user".equals(user.getUsername())
                        && "User".equals(user.getLastName())
                        && "bcrypt-hash".equals(user.getPassword())
                        && user.getRoles().stream()
                        .anyMatch(role -> Roles.ROLE_MANAGER.equals(role.getName()))));
        verify(offices).save(argThat(office -> "Raleigh".equals(office.getName())));
        verify(offices).save(argThat(office -> "Garner".equals(office.getName())));
        verify(offices).save(argThat(office -> "Smithfield".equals(office.getName())));
        assertTrue(Files.exists(dataDirectory.resolve("user-initialized.flag")));
    }

    @Test
    void refusesAWeakFirstRunPasswordWithoutCreatingAUser() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        OfficeRepository offices = mock(OfficeRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(users.findByUsername("user")).thenReturn(Optional.empty());
        when(users.findByUsername("manager")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                bootstrap(users, roles, offices, passwordEncoder, "too-short")
                        .run(mock(ApplicationArguments.class)));

        verify(users, never()).save(any(User.class));
        verify(offices, never()).save(any(Office.class));
    }

    @Test
    void existingSharedUserDoesNotRequireTheFirstRunPasswordAgain() throws Exception {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        OfficeRepository offices = mock(OfficeRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(users.findByUsername("user")).thenReturn(Optional.of(new User()));
        when(offices.findByName(any())).thenReturn(Optional.of(new Office()));

        bootstrap(users, roles, offices, passwordEncoder, "")
                .run(mock(ApplicationArguments.class));

        verify(users, never()).save(any(User.class));
        assertTrue(Files.exists(dataDirectory.resolve("user-initialized.flag")));
    }

    @Test
    void migratesLegacyManagerUsernameWithoutChangingThePassword() throws Exception {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        OfficeRepository offices = mock(OfficeRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User legacyManager = new User();
        legacyManager.setUsername("manager");
        legacyManager.setPassword("existing-bcrypt-hash");

        when(users.findByUsername("user")).thenReturn(Optional.empty());
        when(users.findByUsername("manager")).thenReturn(Optional.of(legacyManager));
        when(offices.findByName(any())).thenReturn(Optional.of(new Office()));

        bootstrap(users, roles, offices, passwordEncoder, "")
                .run(mock(ApplicationArguments.class));

        verify(users).save(argThat(user ->
                user == legacyManager
                        && "user".equals(user.getUsername())
                        && "User".equals(user.getLastName())
                        && "existing-bcrypt-hash".equals(user.getPassword())));
        verify(passwordEncoder, never()).encode(any());
        assertTrue(Files.exists(dataDirectory.resolve("user-initialized.flag")));
    }

    private PortableManagerBootstrap bootstrap(
            UserRepository users,
            RoleRepository roles,
            OfficeRepository offices,
            PasswordEncoder passwordEncoder,
            String password) {
        return new PortableManagerBootstrap(
                users, roles, offices, passwordEncoder,
                "user", password, dataDirectory.toString());
    }
}
