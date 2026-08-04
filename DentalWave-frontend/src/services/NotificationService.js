import axios from 'axios'
import { getAuthHeader } from './AuthService'
import { apiUrl } from './apiConfig'

/**
 * Base URL for Notification endpoints.
 */
const NOTIFICATION_REST_API_BASE_URL = apiUrl('/api/notifications')

/**
 * Gets the count of unread notifications for a user.
 * Used to show the notification badge.
 *
 * @param userId the id of the user
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getUnreadCount = (userId) =>
    axios.get(`${NOTIFICATION_REST_API_BASE_URL}/user/${userId}/unread/count`, getAuthHeader())

/**
 * Gets all unread notifications for a user.
 *
 * @param userId the id of the user
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getUnreadNotifications = (userId) =>
    axios.get(`${NOTIFICATION_REST_API_BASE_URL}/user/${userId}/unread`, getAuthHeader())

/**
 * Gets all notifications for a user.
 *
 * @param userId the id of the user
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getAllNotifications = (userId) =>
    axios.get(`${NOTIFICATION_REST_API_BASE_URL}/user/${userId}`, getAuthHeader())

/**
 * Marks a single notification as read.
 *
 * @param notificationId the id of the notification
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const markAsRead = (notificationId) =>
    axios.patch(`${NOTIFICATION_REST_API_BASE_URL}/${notificationId}/read`, null, getAuthHeader())

/**
 * Marks all notifications as read for a user.
 *
 * @param userId the id of the user
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const markAllAsRead = (userId) =>
    axios.patch(`${NOTIFICATION_REST_API_BASE_URL}/user/${userId}/read-all`, null, getAuthHeader())
