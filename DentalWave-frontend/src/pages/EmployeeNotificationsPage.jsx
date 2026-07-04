import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import EmployeeHeader from '../components/EmployeeHeader'
import { getLoggedInUserId } from '../services/AuthService'
import { getAllNotifications, markAsRead } from '../services/NotificationService'

/**
 * UC12 — Employee Receives Schedule Update Notification.
 *
 * Lists the employee's schedule notifications (New Schedule / Schedule Update),
 * lets them open a notification to view its details (which marks it as read),
 * and links out to the calendar view for the full monthly overview.
 */
function EmployeeNotificationsPage() {
    const navigate = useNavigate()
    const [notifications, setNotifications] = useState([])
    const [expandedId, setExpandedId] = useState(null)
    const [error, setError] = useState('')

    const userId = getLoggedInUserId()

    useEffect(() => { loadNotifications() }, [])

    function loadNotifications() {
        if (!userId) {
            setError('You must be logged in to view notifications.')
            return
        }
        getAllNotifications(userId)
            .then((response) => {
                // UC12 covers schedule notifications only.
                const scheduleNotifications = (response.data || [])
                    .filter((n) => n.type === 'NEW_SCHEDULE' || n.type === 'SCHEDULE_UPDATE')
                    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                setNotifications(scheduleNotifications)
            })
            .catch((err) => {
                console.error(err)
                setError('Unable to load notifications.')
            })
    }

    // Map the backend enum to a human-readable label.
    function typeLabel(type) {
        return type === 'SCHEDULE_UPDATE' ? 'Schedule Update' : 'New Schedule'
    }

    // Format LocalDateTime ("2026-06-15T10:23:00") -> "6/15/2026, 10:23 AM"
    function formatSentDateTime(dateTimeStr) {
        if (!dateTimeStr) return ''
        const date = new Date(dateTimeStr)
        if (isNaN(date.getTime())) return dateTimeStr
        return date.toLocaleString([], {
            year: 'numeric', month: 'numeric', day: 'numeric',
            hour: 'numeric', minute: '2-digit',
        })
    }

    function handleOpen(notification) {
        // Toggle the detail panel open/closed.
        const opening = expandedId !== notification.id
        setExpandedId(opening ? notification.id : null)

        // UC12: opening an unread notification marks it as read.
        if (opening && !notification.read) {
            markAsRead(notification.id)
                .then(() => {
                    setNotifications((prev) =>
                        prev.map((n) =>
                            n.id === notification.id ? { ...n, read: true } : n
                        )
                    )
                })
                .catch((err) => console.error('Failed to mark notification as read', err))
        }
    }

    return (
        <div className="employee-requests-page">
            <EmployeeHeader />

            <main className="requests-content">
                <h1 className="page-title">Notifications</h1>
                <hr className="page-title-underline" />

                {error && <p className="error-message">{error}</p>}

                {notifications.length > 0 ? (
                    <div className="notifications-list">
                        {notifications.map((notification) => (
                            <div
                                key={notification.id}
                                className={`notification-card ${notification.read ? 'read' : 'unread'}`}
                            >
                                <div
                                    className="notification-card-header"
                                    onClick={() => handleOpen(notification)}
                                >
                                    <div className="notification-card-title">
                                        {!notification.read && (
                                            <span className="unread-dot" aria-label="Unread" />
                                        )}
                                        <span className="notification-type-tag">
                                            {typeLabel(notification.type)}
                                        </span>
                                        <span className="notification-message">
                                            {notification.message}
                                        </span>
                                    </div>
                                    <div className="notification-card-meta">
                                        <span className="notification-status">
                                            {notification.read ? 'Read' : 'Unread'}
                                        </span>
                                        <span className="notification-sent">
                                            {formatSentDateTime(notification.createdAt)}
                                        </span>
                                        <span className="notification-chevron">
                                            {expandedId === notification.id ? '▲' : '▼'}
                                        </span>
                                    </div>
                                </div>

                                {expandedId === notification.id && (
                                    <div className="notification-card-detail">
                                        <p><strong>Type:</strong> {typeLabel(notification.type)}</p>
                                        <p><strong>Sent:</strong> {formatSentDateTime(notification.createdAt)}</p>
                                        <p><strong>Sent by:</strong> Scheduling system (on behalf of your manager)</p>
                                        <p>{notification.message}</p>
                                        <button
                                            className="new-request-btn"
                                            onClick={() => navigate('/employee/calendar')}
                                        >
                                            View Calendar
                                        </button>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                ) : (
                    !error && (
                        <p className="empty-requests-message">
                            You have no schedule notifications at this time.
                        </p>
                    )
                )}
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default EmployeeNotificationsPage
