import axios from 'axios'
import { getAuthHeader } from './AuthService'
import { apiUrl } from './apiConfig'

const OFFICE_REST_API_BASE_URL = apiUrl('/api/offices')

export const getAllOffices = () =>
    axios.get(OFFICE_REST_API_BASE_URL, getAuthHeader())

export const getOfficeById = (officeId) =>
    axios.get(`${OFFICE_REST_API_BASE_URL}/${officeId}`, getAuthHeader())

export const createOffice = (office) =>
    axios.post(OFFICE_REST_API_BASE_URL, office, getAuthHeader())

export const updateOffice = (officeId, office) =>
    axios.put(`${OFFICE_REST_API_BASE_URL}/${officeId}`, office, getAuthHeader())

export const deleteOffice = (officeId) =>
    axios.delete(`${OFFICE_REST_API_BASE_URL}/${officeId}`, getAuthHeader())
