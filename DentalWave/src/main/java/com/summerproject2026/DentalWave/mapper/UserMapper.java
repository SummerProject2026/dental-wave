package com.summerproject2026.DentalWave.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.entity.Role;
import com.summerproject2026.DentalWave.entity.User;

/**
 * Mapper class for converting between User entities and UserDto objects.
 */
public class UserMapper {

    /**
     * Maps a User entity to a UserDto.
     *
     * @param user the User entity to be converted
     * @return the corresponding UserDto
     */
    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setEnabled(user.getEnabled());

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        userDto.setRoles(roleNames);

        return userDto;
    }

    /**
     * Maps a UserDto to a User entity.
     *
     * Password and roles are handled separately in the service layer
     * because UserDto does not contain a password and roles must be
     * looked up from the RoleRepository.
     *
     * @param userDto the UserDto to convert
     * @return the corresponding User entity
     */
    public static User toEntity(UserDto userDto) {
        User user = new User();

        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setEnabled(userDto.getEnabled());

        return user;
    }

    /**
     * Updates an existing User entity using values from a UserDto.
     *
     * This is useful for update operations because it preserves fields
     * that should not be replaced, such as password and database-managed
     * relationships.
     *
     * @param userDto the DTO containing updated user information
     * @param user the existing User entity to update
     */
    public static void updateEntityFromDto(UserDto userDto, User user) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setEnabled(userDto.getEnabled());
    }
}