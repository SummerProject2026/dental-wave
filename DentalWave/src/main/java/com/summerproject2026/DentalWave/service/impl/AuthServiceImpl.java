package com.summerproject2026.DentalWave.service.impl;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.summerproject2026.DentalWave.dto.ForgotCredentialsDto;
import com.summerproject2026.DentalWave.dto.JwtAuthResponse;
import com.summerproject2026.DentalWave.dto.LoginDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.entity.Role;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.exception.DuplicateResourceException;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.UserMapper;
import com.summerproject2026.DentalWave.repository.RoleRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.security.JwtTokenProvider;
import com.summerproject2026.DentalWave.service.AuthService;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for authentication and registration operations.
 *
 * Responsibilities:
 * <ul>
 *   <li>Authenticate users using email and password.</li>
 *   <li>Generate JWT tokens after successful login.</li>
 *   <li>Register new users with an encoded password.</li>
 *   <li>Assign a default role to newly registered users.</li>
 *   <li>Handle forgot-username and forgot-password recovery flows.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** Authentication manager used to authenticate login credentials. */
    private final AuthenticationManager authenticationManager;

    /** Repository for User persistence and lookup operations. */
    private final UserRepository userRepository;

    /** Repository for Role lookup operations. */
    private final RoleRepository roleRepository;

    /** Password encoder used to hash passwords before saving users. */
    private final PasswordEncoder passwordEncoder;

    /** JWT token provider used to generate tokens after authentication. */
    private final JwtTokenProvider jwtTokenProvider;

    /** Repository for Employee persistence and lookup operations. */
    private final EmployeeRepository employeeRepository;

    /** Mail sender used for credential recovery emails. */
    private final JavaMailSender mailSender;

    /** Characters used when generating temporary passwords. */
    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    /** Length of generated temporary passwords. */
    private static final int TEMP_PASSWORD_LENGTH = 10;

    /**
     * Authenticates a user using their username or email and password.
     *
     * @param loginDto the login credentials entered by the user
     * @return a JWT authentication response containing token and user details
     */
    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        String loginIdentifier = loginDto.getUsername().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginIdentifier,
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginIdentifier)
                .or(() -> userRepository.findByEmail(loginIdentifier))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email or username : " + loginIdentifier));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        // Get the user's primary role
        String role = user.getRoles()
                .stream()
                .findFirst()
                .map(Role::getName)
                .orElse(null);

        // Build the JWT response with all user details
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setTokenType("Bearer");
        jwtAuthResponse.setRole(role);
        jwtAuthResponse.setEmail(user.getEmail());
        jwtAuthResponse.setUsername(user.getUsername());
        jwtAuthResponse.setFirstName(user.getFirstName());
        jwtAuthResponse.setLastName(user.getLastName());
        jwtAuthResponse.setUserId(user.getId());

        Optional<Employee> employee = employeeRepository.findByUserId(user.getId());

        employee.ifPresent(value ->
                jwtAuthResponse.setEmployeeId(value.getId())
        );

        return jwtAuthResponse;
    }

    /**
     * Registers a new user account with default ROLE_ASSISTANT.
     *
     * @param registerDto the registration information for the new user
     * @return the newly registered user as a UserDto
     * @throws DuplicateResourceException if the email is already in use
     * @throws ResourceNotFoundException if the default role is missing
     */
    @Override
    public UserDto register(RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new DuplicateResourceException(
                    "A user with email '" + registerDto.getEmail() + "' already exists.");
        }

        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPhoneNumber(registerDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();

        Role defaultRole = roleRepository.findByName("ROLE_ASSISTANT")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: ROLE_ASSISTANT"));

        roles.add(defaultRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return UserMapper.toDto(savedUser);
    }

    /**
     * Registers a new user account with a specific role.
     * Used by admin to create HR users.
     *
     * @param registerDto the registration information for the new user
     * @param roleName the role name to assign e.g. ROLE_HR
     * @return the newly registered user as a UserDto
     * @throws DuplicateResourceException if the email or username is already in use
     * @throws ResourceNotFoundException if the role is not found
     */
    @Override
    public UserDto registerWithRole(RegisterDto registerDto, String roleName) {

        // Check for duplicate email
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new DuplicateResourceException(
                    "A user with email '" + registerDto.getEmail() + "' already exists.");
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + registerDto.getUsername() + "' is already taken.");
        }

        // Build the new user
        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPhoneNumber(registerDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEnabled(true);

        // Find and assign the specified role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        // Save and return the new user
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    // -------------------------------------------------------------------------
    // Forgot username / password recovery flows
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random temporary password, encodes and saves it,
     * then emails the plain temporary password to the user. If the
     * provided details do not match any account, nothing happens.</p>
     */
    @Override
    public void forgotPassword(ForgotCredentialsDto forgotDto) {
        User user = findMatchingUser(forgotDto);
        if (user == null) {
            log.info("Forgot-password request did not match any account.");
            return;
        }

        // Generate and save a new temporary password
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // Email the plain temporary password to the user
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(user.getEmail());
            email.setSubject("DentalWave — Your Temporary Password");
            email.setText(
                    "Dear " + user.getFirstName() + ",\n\n" +
                            "A password reset was requested for your account.\n\n" +
                            "Your temporary password is: " + tempPassword + "\n\n" +
                            "Please log in with this temporary password and change it " +
                            "immediately from your profile page.\n\n" +
                            "If you did not request this reset, please contact HR.\n\n" +
                            "DentalWave System"
            );
            mailSender.send(email);
            log.info("Temporary password email sent to user {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send temporary password email to user {}: {}",
                    user.getId(), e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Emails the user their username. If the provided details do not
     * match any account, nothing happens.</p>
     */
    @Override
    public void forgotUsername(ForgotCredentialsDto forgotDto) {
        User user = findMatchingUser(forgotDto);
        if (user == null) {
            log.info("Forgot-username request did not match any account.");
            return;
        }

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(user.getEmail());
            email.setSubject("DentalWave — Your Username");
            email.setText(
                    "Dear " + user.getFirstName() + ",\n\n" +
                            "A username reminder was requested for your account.\n\n" +
                            "Your username is: " + user.getUsername() + "\n\n" +
                            "If you did not request this reminder, please contact HR.\n\n" +
                            "DentalWave System"
            );
            mailSender.send(email);
            log.info("Username reminder email sent to user {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send username reminder email to user {}: {}",
                    user.getId(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Finds the user whose email, first name, and last name all match
     * the provided details (case-insensitive, trimmed). Returns null
     * if any field is missing or no full match is found.
     *
     * @param forgotDto the details to verify
     * @return the matching User, or null if no full match
     */
    private User findMatchingUser(ForgotCredentialsDto forgotDto) {
        if (forgotDto == null
                || forgotDto.getEmail() == null
                || forgotDto.getFirstName() == null
                || forgotDto.getLastName() == null) {
            return null;
        }

        return userRepository.findByEmail(forgotDto.getEmail().trim())
                .filter(user -> user.getFirstName() != null
                        && user.getFirstName().trim()
                        .equalsIgnoreCase(forgotDto.getFirstName().trim()))
                .filter(user -> user.getLastName() != null
                        && user.getLastName().trim()
                        .equalsIgnoreCase(forgotDto.getLastName().trim()))
                .orElse(null);
    }

    /**
     * Generates a random temporary password using letters and digits
     * that are hard to confuse (no 0/O, 1/l/I).
     *
     * @return a random temporary password
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(
                    random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
