import { API_BASE_URL, authHeaders } from './api'

/**
 * Notification record as returned by the backend (NotificationDto).
 */
export type Notification = {
    id: number
    recipientId: number
    recipientUsername: string
    message: string
    read: boolean
    createdAt: string
    type: string
    targetTab: string
    referenceId: number | null
}

/** Fetches all notifications for a user. */
export async function getAllNotifications(userId: number): Promise<Notification[]> {
    const res = await fetch(`${API_BASE_URL}/api/notifications/user/${userId}`, {
        headers: authHeaders(),
    })
    if (!res.ok) throw new Error(`Failed to load notifications (${res.status})`)
    return res.json()
}

/** Marks a single notification as read. */
export async function markAsRead(notificationId: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/api/notifications/${notificationId}/read`, {
        method: 'PATCH',
        headers: authHeaders(),
    })
    if (!res.ok) throw new Error(`Failed to mark notification as read (${res.status})`)
}
