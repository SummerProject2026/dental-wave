import axios from 'axios'
import { getAuthHeader } from './AuthService'
import { apiUrl } from './apiConfig'
const RULES=apiUrl('/api/doctor-work-rules'), ROTATIONS=apiUrl('/api/schedule-rotation-groups')
export const getDoctorRules=id=>axios.get(`${RULES}/doctor/${id}`,getAuthHeader())
const patternConfig=()=>({...getAuthHeader(),skipAuthRedirect:true})
export const createDoctorRule=value=>axios.post(RULES,value,patternConfig())
export const replaceDoctorRules=(doctorId,value)=>axios.put(`${RULES}/doctor/${doctorId}`,value,patternConfig())
export const deleteDoctorRule=id=>axios.delete(`${RULES}/${id}`,patternConfig())
export const previewDoctorRules=(id,startDate)=>axios.get(`${RULES}/doctor/${id}/preview`,{...getAuthHeader(),params:{startDate,days:28}})
export const getRotationGroups=()=>axios.get(ROTATIONS,getAuthHeader())
export const createRotationGroup=value=>axios.post(ROTATIONS,value,patternConfig())
