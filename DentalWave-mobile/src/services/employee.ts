import { API_BASE_URL, authHeaders } from './api'

export type EmployeeProfile = {
    id: number
    firstName: string
    lastName: string
    username: string
    email: string
    phoneNumber: string | null
    position: string
    status: string
    hireDate: string
    timeOff: number
}

export async function getEmployeeById(employeeId: number): Promise<EmployeeProfile> {
    const res = await fetch(`${API_BASE_URL}/api/employees/${employeeId}`, {
        headers: authHeaders(),
    })
    if (!res.ok) throw new Error(`Failed to load profile (${res.status})`)
    return res.json()
}
