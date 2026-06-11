import axios from 'axios'

/**
 * Base URL for Time Off Request endpoints.
 */
const TIME_OFF_REQUEST_REST_API_BASE_URL = 'http://localhost:8080/api/time-off-requests'

/**
 * Retrieves all time off requests for one employee.
 *
 * @param employeeId Employee id
 * @returns Promise<axios.AxiosResponse<any>>
 */
export const getTimeOffRequestsByEmployee = (employeeId) =>
    axios.get(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/employee/${employeeId}`)

/**
 * Retrieves one time off request by id.
 *
 * @param requestId Time off request id
 * @returns TimeOffRequestDto
 */
export const getTimeOffRequestById = (requestId) =>
    axios.get(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}`)

/**
 * Deletes a time off request.
 *
 * @param requestId Time off request id
 */
export const deleteTimeOffRequest = (requestId) =>
    axios.delete(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}`)