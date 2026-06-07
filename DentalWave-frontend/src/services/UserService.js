import axios from 'axios'

/**
 * Base URL for User endpoints.
 */
const USER_REST_API_BASE_URL = 'http://localhost:8080/api/users'

/**
 * Creates a new user.
 *
 * @param user UserDto
 * @returns Created UserDto
 */
export const createUser = (user) =>
    axios.post(USER_REST_API_BASE_URL, user)

/**
 * Retrieves a user by id.
 *
 * @param id User id
 * @returns UserDto
 */
export const getUserById = (id) =>
    axios.get(`${USER_REST_API_BASE_URL}/${id}`)

/**
 * Retrieves all users.
 *
 * @returns List<UserDto>
 */
export const getAllUsers = () =>
    axios.get(USER_REST_API_BASE_URL)

/**
 * Updates a user.
 *
 * @param id User id
 * @param user Updated UserDto
 * @returns Updated UserDto
 */
export const updateUser = (id, user) =>
    axios.put(`${USER_REST_API_BASE_URL}/${id}`, user)

/**
 * Deletes a user.
 *
 * @param id User id
 */
export const deleteUser = (id) =>
    axios.delete(`${USER_REST_API_BASE_URL}/${id}`)

/**
 * Retrieves a user by email.
 *
 * @param email User email
 * @returns UserDto
 */
export const getUserByEmail = (email) =>
    axios.get(`${USER_REST_API_BASE_URL}/email/${email}`)

/**
 * Retrieves users by role.
 *
 * @param role Role name
 * @returns List<UserDto>
 */
export const getUsersByRole = (role) =>
    axios.get(`${USER_REST_API_BASE_URL}/role/${role}`)

/**
 * Searches users by keyword.
 *
 * Example:
 * searchUsers("smith")
 *
 * @param keyword Search term
 * @returns List<UserDto>
 */
export const searchUsers = (keyword) =>
    axios.get(`${USER_REST_API_BASE_URL}/search?keyword=${keyword}`)

/**
 * Enables a user account.
 *
 * @param id User id
 * @returns Updated UserDto
 */
export const enableUser = (id) =>
    axios.put(`${USER_REST_API_BASE_URL}/${id}/enable`)

/**
 * Disables a user account.
 *
 * @param id User id
 * @returns Updated UserDto
 */
export const disableUser = (id) =>
    axios.put(`${USER_REST_API_BASE_URL}/${id}/disable`)