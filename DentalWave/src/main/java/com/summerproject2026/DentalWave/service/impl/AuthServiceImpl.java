package com.summerproject2026.DentalWave.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for authentication and registration operations.
 *
 * Responsibilities:
 * <ul>
 *   <li>Authenticate users using email and password.</li>
 *   <li>Generate JWT tokens after successful login.</li>
 *   <li>Register new users with an encoded password.</li>
 *   <li>Assign a default role to newly registered users.</li>
 * </ul>
 */
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

    /** Mapper used to convert User entities into UserDto objects. */
    //private final UserMapper userMapper;

    /**
     * Authenticates a user using their username or email and password.
     *
     * @param loginDto the login credentials entered by the user
     * @return a JWT authentication response containing token and user details
     */
    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginDto.getUsername())
                .or(() -> userRepository.findByEmail(loginDto.getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email or username : " + loginDto.getUsername()));

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
        jwtAuthResponse.setId(user.getId());

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
}