import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { logout, getLoggedInUserId } from '../services/AuthService'
import { getUnreadNotifications, markAsRead } from '../services/NotificationService'

function ManagerHeader() {
    const navigate = useNavigate()
    const [notifications, setNotifications] = useState([])
    const [showDropdown, setShowDropdown] = useState(false)

    function loadNotifications() {
        const userId = getLoggedInUserId()
        if (!userId) return

        getUnreadNotifications(userId)
            .then((response) => setNotifications(response.data || []))
            .catch((err) => console.error('Failed to load notifications', err))
    }

    useEffect(() => {
        loadNotifications()
    }, [])

    function handleBellClick() {
        setShowDropdown((prev) => !prev)
    }

    function handleNotificationClick(notification) {
        markAsRead(notification.id)
            .then(() => {
                setNotifications((prev) =>
                    prev.filter((n) => n.id !== notification.id)
                )
            })
            .catch((err) => console.error('Failed to mark notification as read', err))
    }

    function handleLogout() {
        logout()
        navigate('/login')
    }

    const totalUnreadCount = notifications.length

    return (
        <header className="calendar-header">
            <Link to="/manager/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/manager/calendar/build">Calendar</Link>
                <Link to="/manager/requests">Approved Request</Link>
                <Link to="/manager/employees">Employees</Link>
            </nav>

            <div className="header-actions">

                <div className="notification-bell-wrapper">
                    <button className="notification-bell-btn" onClick={handleBellClick}>
                        🔔
                        {totalUnreadCount > 0 && (
                            <span className="notification-badge bell-badge">
                                {totalUnreadCount}
                            </span>
                        )}
                    </button>

                    {showDropdown && (
                        <div className="notification-dropdown">
                            {notifications.length > 0 ? (
                                notifications.map((notification) => (
                                    <div
                                        key={notification.id}
                                        className="notification-item"
                                        onClick={() => handleNotificationClick(notification)}
                                    >
                                        {notification.message}
                                    </div>
                                ))
                            ) : (
                                <div className="notification-item-empty">
                                    No new notifications
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <Link to="/manager/profile" className="user-icon">👤</Link>
                <button className="logout-button" onClick={handleLogout}>Logout</button>
            </div>
        </header>
    )
}

export default ManagerHeader