import axios from 'axios'
import { getAuthHeader } from './AuthService'
import { apiUrl } from './apiConfig'

/**
 * Base URL for Calendar endpoints.
 */
const CALENDAR_REST_API_BASE_URL = apiUrl('/api/calendars')

/**
 * Creates a new calendar.
 *
 * @param calendar the calendar data to create
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const createCalendar = (calendar) =>
    axios.post(CALENDAR_REST_API_BASE_URL, calendar, getAuthHeader())

/**
 * Gets a single calendar by id.
 *
 * @param calendarId the calendar id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getCalendarById = (calendarId) =>
    axios.get(`${CALENDAR_REST_API_BASE_URL}/${calendarId}`, getAuthHeader())

/**
 * Gets all calendars.
 *
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getAllCalendars = () =>
    axios.get(CALENDAR_REST_API_BASE_URL, getAuthHeader())

/**
 * Updates an existing calendar.
 *
 * @param calendarId the calendar id
 * @param calendar the updated calendar data
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const updateCalendar = (calendarId, calendar) =>
    axios.put(`${CALENDAR_REST_API_BASE_URL}/${calendarId}`, calendar, getAuthHeader())

/**
 * Deletes a calendar.
 *
 * @param calendarId the calendar id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const deleteCalendar = (calendarId) =>
    axios.delete(`${CALENDAR_REST_API_BASE_URL}/${calendarId}`, getAuthHeader())

/**
 * Publishes a calendar, making it visible to staff.
 *
 * @param calendarId the calendar id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const publishCalendar = (calendarId) =>
    axios.post(`${CALENDAR_REST_API_BASE_URL}/${calendarId}/publish`, null, getAuthHeader())

/**
 * Reverts a calendar to draft state.
 *
 * @param calendarId the calendar id
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const unpublishCalendar = (calendarId) =>
    axios.post(`${CALENDAR_REST_API_BASE_URL}/${calendarId}/unpublish`, null, getAuthHeader())

/**
 * Gets all calendars for a given month label.
 *
 * @param month URL-encoded month label, e.g. "June 2026"
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getCalendarsByMonth = (month) =>
    axios.get(`${CALENDAR_REST_API_BASE_URL}/month/${encodeURIComponent(month)}`, getAuthHeader())

/**
 * Gets all published calendars.
 *
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getPublishedCalendars = () =>
    axios.get(`${CALENDAR_REST_API_BASE_URL}/published`, getAuthHeader())

/**
 * Adds a new schedule to a calendar.
 *
 * @param calendarId the parent calendar id
 * @param schedule the schedule data to add
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const addScheduleToCalendar = (calendarId, schedule) =>
    axios.post(`${CALENDAR_REST_API_BASE_URL}/${calendarId}/schedules`, schedule, getAuthHeader())

/**
 * Removes a schedule from a calendar.
 *
 * @param calendarId the parent calendar id
 * @param scheduleId the schedule id to remove
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const removeScheduleFromCalendar = (calendarId, scheduleId) =>
    axios.delete(
        `${CALENDAR_REST_API_BASE_URL}/${calendarId}/schedules/${scheduleId}`,
        getAuthHeader()
    )

/**
 * Auto-generates a draft calendar for the given office and month.
 * Creates one schedule per weekday with employees automatically
 * assigned to teams based on role (Doctor + TC + Assistants).
 *
 * @param calendar the office, month, date range, and creator info
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const generateCalendar = (calendar) =>
    axios.post(`${CALENDAR_REST_API_BASE_URL}/generate`, calendar, getAuthHeader())

/**
 * Schedules an employee across every day in the given calendar.
 * The employee is added to the smallest team on each day.
 *
 * @param calendarId the calendar to schedule the employee into
 * @param employeeId the employee to schedule
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const scheduleEmployeeAcrossCalendar = (calendarId, employeeId) =>
    axios.post(
        `${CALENDAR_REST_API_BASE_URL}/${calendarId}/employees/${employeeId}/schedule-all`,
        null,
        getAuthHeader()
    )

/**
 * Removes an employee from every team across every day in the calendar.
 *
 * @param calendarId the calendar to remove the employee from
 * @param employeeId the employee to remove
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const removeEmployeeFromCalendar = (calendarId, employeeId) =>
    axios.delete(
        `${CALENDAR_REST_API_BASE_URL}/${calendarId}/employees/${employeeId}/schedule-all`,
        getAuthHeader()
    )

/**
 * Removes an employee from every team on schedules within a date range
 * for the given office. Used by Manager "Approved Request" page.
 *
 * @param officeId the office whose calendars to search
 * @param employeeId the employee to remove
 * @param startDate ISO date string e.g. '2026-06-15'
 * @param endDate ISO date string e.g. '2026-06-20'
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const removeEmployeeFromScheduleOnDates = (officeId, employeeId, startDate, endDate) =>
    axios.delete(
        `${CALENDAR_REST_API_BASE_URL}/office/${officeId}/employees/${employeeId}/dates?startDate=${startDate}&endDate=${endDate}`,
        getAuthHeader()
    )
