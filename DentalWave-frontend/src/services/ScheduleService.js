import axios from 'axios'
import { getAuthHeader } from './AuthService'

/**
 * Base URL for Schedule endpoints.
 */
const SCHEDULE_REST_API_BASE_URL = 'http://localhost:8080/api/schedules'

/**
 * Creates a new schedule.
 *
 * @param schedule the schedule data to create
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const createSchedule = (schedule) =>
    axios.post(SCHEDULE_REST_API_BASE_URL, schedule, getAuthHeader())

/**
 * Gets a single schedule by id.
 *
 * @param scheduleId the schedule id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getScheduleById = (scheduleId) =>
    axios.get(`${SCHEDULE_REST_API_BASE_URL}/${scheduleId}`, getAuthHeader())

/**
 * Gets all schedules.
 *
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getAllSchedules = () =>
    axios.get(SCHEDULE_REST_API_BASE_URL, getAuthHeader())

/**
 * Updates an existing schedule.
 *
 * @param scheduleId the schedule id
 * @param schedule the updated schedule data
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const updateSchedule = (scheduleId, schedule) =>
    axios.put(`${SCHEDULE_REST_API_BASE_URL}/${scheduleId}`, schedule, getAuthHeader())

/**
 * Deletes a schedule.
 *
 * @param scheduleId the schedule id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const deleteSchedule = (scheduleId) =>
    axios.delete(`${SCHEDULE_REST_API_BASE_URL}/${scheduleId}`, getAuthHeader())

/**
 * Gets all schedules for a specific date.
 *
 * @param date ISO date string e.g. '2026-06-29'
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getSchedulesByDate = (date) =>
    axios.get(`${SCHEDULE_REST_API_BASE_URL}/date/${date}`, getAuthHeader())

/**
 * Gets all schedules for a specific calendar.
 *
 * @param calendarId the calendar id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getSchedulesByCalendar = (calendarId) =>
    axios.get(`${SCHEDULE_REST_API_BASE_URL}/calendar/${calendarId}`, getAuthHeader())

/**
 * Gets all published schedules for a specific employee.
 * Used by UC2 — Employee Views Personal Calendar.
 *
 * @param employeeId the employee id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getSchedulesByEmployee = (employeeId) =>
    axios.get(`${SCHEDULE_REST_API_BASE_URL}/employee/${employeeId}`, getAuthHeader())

/**
 * Gets all published schedules for a specific employee by name.
 * Used by UC2 — Employee Views Personal Calendar.
 *
 * @param employeeName the employee's full name
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getSchedulesByEmployeeName = (employeeName) =>
    axios.get(`${SCHEDULE_REST_API_BASE_URL}/employee/name/${employeeName}`, getAuthHeader())

/**
 * Assigns an employee to a team lead's team on a schedule.
 *
 * @param scheduleId the schedule id
 * @param userId the team lead (User) id
 * @param employeeId the employee id to assign
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const assignEmployeeToTeam = (scheduleId, userId, employeeId) =>
    axios.post(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${userId}/employees/${employeeId}`,
        null,
        getAuthHeader()
    )

/**
 * Removes an employee from a team lead's team on a schedule.
 *
 * @param scheduleId the schedule id
 * @param userId the team lead (User) id
 * @param employeeId the employee id to remove
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const removeEmployeeFromTeam = (scheduleId, userId, employeeId) =>
    axios.delete(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${userId}/employees/${employeeId}`,
        getAuthHeader()
    )

/**
 * Publishes a schedule.
 *
 * @param scheduleId the schedule id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const publishSchedule = (scheduleId) =>
    axios.patch(`${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/publish`, null, getAuthHeader())