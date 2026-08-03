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
 * Assigns an employee to a specific team within a schedule.
 *
 * @param scheduleId the schedule the team belongs to
 * @param teamId the id of the team to add the employee to
 * @param employeeId the employee to add
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const assignEmployeeToTeam = (scheduleId, teamId, employeeId) =>
    axios.post(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${teamId}/employees/${employeeId}`,
        null,
        getAuthHeader()
    )

/**
 * Removes an employee from a specific team within a schedule.
 *
 * @param scheduleId the schedule the team belongs to
 * @param teamId the id of the team to remove the employee from
 * @param employeeId the employee to remove
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const removeEmployeeFromTeam = (scheduleId, teamId, employeeId) =>
    axios.delete(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${teamId}/employees/${employeeId}`,
        getAuthHeader()
    )

export const assignResourceToTeam = (scheduleId, teamId, resourceId) =>
    axios.post(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${teamId}/resources/${resourceId}`,
        null,
        getAuthHeader()
    )

export const removeResourceFromTeam = (scheduleId, teamId, resourceId) =>
    axios.delete(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${teamId}/resources/${resourceId}`,
        getAuthHeader()
    )

export const updateEmployeePartialDayNote = (scheduleId, teamId, employeeId, note) =>
    axios.put(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${teamId}/employees/${employeeId}/partial-day`,
        { note },
        getAuthHeader()
    )

export const updateResourcePartialDayNote = (scheduleId, teamId, resourceId, note) =>
    axios.put(
        `${SCHEDULE_REST_API_BASE_URL}/${scheduleId}/teams/${teamId}/resources/${resourceId}/partial-day`,
        { note },
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

/**
 * Creates a named team for a schedule.
 *
 * @param scheduleId the schedule id
 * @param name the team label/name
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const createTeam = (scheduleId, name) =>
    axios.post(
        'http://localhost:8080/api/schedule-teams',
        { name, scheduleId, employees: [] },
        getAuthHeader()
    )

/**
 * Renames a schedule team.
 *
 * @param teamId the team id
 * @param name the new name
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const renameTeam = (teamId, name) =>
    axios.put(
        `http://localhost:8080/api/schedule-teams/${teamId}/name`,
        null,
        { ...getAuthHeader(), params: { name } }
    )

/**
 * Deletes a schedule team.
 *
 * @param teamId the team id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const deleteTeam = (teamId) =>
    axios.delete(`http://localhost:8080/api/schedule-teams/${teamId}`, getAuthHeader())
