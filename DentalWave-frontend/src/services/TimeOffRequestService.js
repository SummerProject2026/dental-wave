import axios from 'axios'
import { getAuthHeader } from './AuthService'

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
    axios.get(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/employee/${employeeId}`, getAuthHeader())

/**
 * Retrieves one time off request by id.
 *
 * @param requestId Time off request id
 * @returns TimeOffRequestDto
 */
export const getTimeOffRequestById = (requestId) =>
    axios.get(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}`, getAuthHeader())

/**
 * Deletes a time off request.
 *
 * @param requestId Time off request id
 */
export const deleteTimeOffRequest = (requestId) =>
    axios.delete(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}`, getAuthHeader())


/**
 * Gets all the time off requests
 *
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getAllTimeOffRequests = () =>
    axios.get(TIME_OFF_REQUEST_REST_API_BASE_URL, getAuthHeader())


/**
 * Creates the time off request.
 *
 * @param request
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const createTimeOffRequest = (request) =>
    axios.post(TIME_OFF_REQUEST_REST_API_BASE_URL, request, getAuthHeader())

/**
 * Updates a pending time off request.
 *
 * @param requestId Time off request id
 * @param request Updated time off request data
 * @returns Updated TimeOffRequestDto
 */
export const updateTimeOffRequest = (requestId, request) =>
    axios.put(`${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}`, request, getAuthHeader())

/**
 * Approves a pending time off request.
 * Used by HR on the Request Detail page (UC6).
 *
 * @param requestId Time off request id
 * @param reviewedById Id of the HR user approving the request
 * @param reviewComment Optional comment from the reviewer
 * @returns Updated TimeOffRequestDto with status APPROVED
 */
export const approveTimeOffRequest = (requestId, reviewedById, reviewComment) =>
    axios.patch(
        `${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}/approve`,
        null,
        {
            ...getAuthHeader(),
            params: { reviewedById, reviewComment }
        }
    )

/**
 * Denies a pending time off request.
 * Used by HR on the Request Detail page (UC6).
 *
 * @param requestId Time off request id
 * @param reviewedById Id of the HR user denying the request
 * @param reviewComment Optional comment from the reviewer
 * @returns Updated TimeOffRequestDto with status DENIED
 */
export const denyTimeOffRequest = (requestId, reviewedById, reviewComment) =>
    axios.patch(
        `${TIME_OFF_REQUEST_REST_API_BASE_URL}/${requestId}/deny`,
        null,
        {
            ...getAuthHeader(),
            params: { reviewedById, reviewComment }
        }
    )