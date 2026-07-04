import { API_BASE_URL, authHeaders } from './api'

export type ScheduleEntry = {
    id: number
    date: string
    startTime: string | null
    endTime: string | null
    notes: string | null
    calendarId: number | null
}

export type CalendarSummary = {
    id: number
    officeId: number | null
}

export async function getSchedulesByEmployee(employeeId: number): Promise<ScheduleEntry[]> {
    const res = await fetch(`${API_BASE_URL}/api/schedules/employee/${employeeId}`, {
        headers: authHeaders(),
    })
    if (!res.ok) throw new Error(`Failed to load schedules (${res.status})`)
    return res.json()
}

export async function getAllCalendars(): Promise<CalendarSummary[]> {
    const res = await fetch(`${API_BASE_URL}/api/calendars`, {
        headers: authHeaders(),
    })
    if (!res.ok) throw new Error(`Failed to load calendars (${res.status})`)
    return res.json()
}
