import { API_BASE_URL, authHeaders } from './api'

export type TimeOffRequest = {
    id: number
    employeeId: number
    startDate: string
    endDate: string | null
    startTime: string | null
    endTime: string | null
    reason: string | null
    status: string
    emergency: boolean | null
    submittedAt: string | null
    reviewedByName: string | null
    reviewComment: string | null
}

export async function getTimeOffRequestsByEmployee(employeeId: number): Promise<TimeOffRequest[]> {
    const res = await fetch(`${API_BASE_URL}/api/time-off-requests/employee/${employeeId}`, {
        headers: authHeaders(),
    })
    if (!res.ok) throw new Error(`Failed to load requests (${res.status})`)
    return res.json()
}

export async function createTimeOffRequest(payload: {
    employeeId: number
    startDate: string
    endDate: string
    startTime: string | null
    endTime: string | null
    reason: string
    emergency: boolean
    status: string
}): Promise<TimeOffRequest> {
    const res = await fetch(`${API_BASE_URL}/api/time-off-requests`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(payload),
    })
    if (!res.ok) throw new Error(`Failed to submit request (${res.status})`)
    return res.json()
}
