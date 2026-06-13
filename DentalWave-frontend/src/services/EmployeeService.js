import axios from 'axios'
import { getAuthHeader } from './AuthService'

/**
 * Base URL for Employee endpoints.
 */
const EMPLOYEE_REST_API_BASE_URL = 'http://localhost:8080/api/employees'

/**
 * Creates a new employee.
 *
 * @param employee EmployeeDto
 * @returns Created EmployeeDto
 */
export const createEmployee = (employee) =>
    axios.post(EMPLOYEE_REST_API_BASE_URL, employee, getAuthHeader())

/**
 * Retrieves an employee by id.
 *
 * @param id Employee id
 * @returns EmployeeDto
 */
export const getEmployeeById = (id) =>
    axios.get(`${EMPLOYEE_REST_API_BASE_URL}/${id}`, getAuthHeader())

/**
 * Retrieves all employees.
 *
 * @returns List<EmployeeDto>
 */
export const getAllEmployees = () =>
    axios.get(EMPLOYEE_REST_API_BASE_URL, getAuthHeader())

/**
 * Updates an employee.
 *
 * @param id Employee id
 * @param employee Updated EmployeeDto
 * @returns Updated EmployeeDto
 */
export const updateEmployee = (id, employee) =>
    axios.put(`${EMPLOYEE_REST_API_BASE_URL}/${id}`, employee, getAuthHeader())

/**
 * Deletes an employee.
 *
 * @param id Employee id
 */
export const deleteEmployee = (id) =>
    axios.delete(`${EMPLOYEE_REST_API_BASE_URL}/${id}`, getAuthHeader())

/**
 * Retrieves employees by office id.
 *
 * @param officeId Office id
 * @returns List<EmployeeDto>
 */
export const getEmployeesByOffice = (officeId) =>
    axios.get(`${EMPLOYEE_REST_API_BASE_URL}/office/${officeId}`, getAuthHeader())

/**
 * Retrieves employees by work status.
 *
 * Example:
 * getEmployeesByStatus("ACTIVE")
 *
 * @param status WorkStatus value
 * @returns List<EmployeeDto>
 */
export const getEmployeesByStatus = (status) =>
    axios.get(`${EMPLOYEE_REST_API_BASE_URL}/status/${status}`, getAuthHeader())

/**
 * Searches employees by keyword.
 *
 * Example:
 * searchEmployees("jane")
 *
 * @param keyword Search term
 * @returns List<EmployeeDto>
 */
export const searchEmployees = (keyword) =>
    axios.get(
        `${EMPLOYEE_REST_API_BASE_URL}/search?keyword=${keyword}`,
        getAuthHeader()
    )

/**
 * Adds availability to an employee.
 *
 * @param employeeId Employee id
 * @param availability AvailabilityDto
 * @returns Created AvailabilityDto
 */
export const addAvailability = (employeeId, availability) =>
    axios.post(
        `${EMPLOYEE_REST_API_BASE_URL}/${employeeId}/availability`,
        availability,
        getAuthHeader()
    )

/**
 * Updates an employee availability record.
 *
 * @param employeeId Employee id
 * @param availabilityId Availability id
 * @param availability Updated AvailabilityDto
 * @returns Updated AvailabilityDto
 */
export const updateAvailability = (employeeId, availabilityId, availability) =>
    axios.put(
        `${EMPLOYEE_REST_API_BASE_URL}/${employeeId}/availability/${availabilityId}`,
        availability,
        getAuthHeader()
    )

/**
 * Deletes an employee availability record.
 *
 * @param employeeId Employee id
 * @param availabilityId Availability id
 */
export const deleteAvailability = (employeeId, availabilityId) =>
    axios.delete(
        `${EMPLOYEE_REST_API_BASE_URL}/${employeeId}/availability/${availabilityId}`,
        getAuthHeader()
    )