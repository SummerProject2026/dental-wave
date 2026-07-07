import axios from 'axios'
import { getAuthHeader } from './AuthService'

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
    axios.post(USER_REST_API_BASE_URL, user, getAuthHeader())

/**
 * Retrieves a user by id.
 *
 * @param id User id
 * @returns UserDto
 */
export const getUserById = (id) =>
    axios.get(`${USER_REST_API_BASE_URL}/${id}`, getAuthHeader())

/**
 * Retrieves the currently logged-in user's profile.
 *
 * @returns UserDto
 */
export const getCurrentUser = () =>
    axios.get(`${USER_REST_API_BASE_URL}/me`, getAuthHeader())

/**
 * Retrieves all users.
 *
 * @returns List<UserDto>
 */
export const getAllUsers = () =>
    axios.get(USER_REST_API_BASE_URL, getAuthHeader())

/**
 * Updates a user.
 *
 * @param id User id
 * @param user Updated UserDto
 * @returns Updated UserDto
 */
export const updateUser = (id, user) =>
    axios.put(`${USER_REST_API_BASE_URL}/${id}`, user, getAuthHeader())

/**
 * Updates the currently logged-in user's editable profile fields.
 *
 * @param user Updated UserDto fields
 * @returns Updated UserDto
 */
export const updateCurrentUser = (user) =>
    axios.put(`${USER_REST_API_BASE_URL}/me`, user, getAuthHeader())

/**
 * Deletes a user.
 *
 * @param id User id
 */
export const deleteUser = (id) =>
    axios.delete(`${USER_REST_API_BASE_URL}/${id}`, getAuthHeader())

/**
 * Retrieves a user by email.
 *
 * @param email User email
 * @returns UserDto
 */
export const getUserByEmail = (email) =>
    axios.get(`${USER_REST_API_BASE_URL}/email/${email}`, getAuthHeader())

/**
 * Retrieves users by role.
 *
 * @param role Role name
 * @returns List<UserDto>
 */
export const getUsersByRole = (role) =>
    axios.get(`${USER_REST_API_BASE_URL}/role/${role}`, getAuthHeader())

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
    axios.get(`${USER_REST_API_BASE_URL}/search?keyword=${keyword}`, getAuthHeader())

/**
 * Enables a user account.
 *
 * @param id User id
 * @returns Updated UserDto
 */
export const enableUser = (id) =>
    axios.put(`${USER_REST_API_BASE_URL}/${id}/enable`, null, getAuthHeader())

/**
 * Disables a user account.
 *
 * @param id User id
 * @returns Updated UserDto
 */
export const disableUser = (id) =>
    axios.put(`${USER_REST_API_BASE_URL}/${id}/disable`, null, getAuthHeader())
