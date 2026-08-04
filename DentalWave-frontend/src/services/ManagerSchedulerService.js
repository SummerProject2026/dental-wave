import axios from 'axios'
import { getAuthHeader } from './AuthService'
import { apiUrl } from './apiConfig'
const API = apiUrl('/api/manager')
export const getResources = (type) => axios.get(`${API}/resources`, { ...getAuthHeader(), params: { type } })
export const createResource = (value) => axios.post(`${API}/resources`, value, getAuthHeader())
export const updateResource = (id, value) => axios.put(`${API}/resources/${id}`, value, getAuthHeader())
export const deleteResource = (id) => axios.delete(`${API}/resources/${id}`, getAuthHeader())
export const getReusableTeams = () => axios.get(`${API}/teams`, getAuthHeader())
export const createReusableTeam = (value) => axios.post(`${API}/teams`, value, getAuthHeader())
export const updateReusableTeam = (id, value) => axios.put(`${API}/teams/${id}`, value, getAuthHeader())
export const duplicateReusableTeam = (id) => axios.post(`${API}/teams/${id}/duplicate`, {}, getAuthHeader())
export const deleteReusableTeam = (id) => axios.delete(`${API}/teams/${id}`, getAuthHeader())
