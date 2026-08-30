
import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router'
import { logout, getLoggedInUserId } from '../services/AuthService'
import { getUnreadNotifications, markAsRead } from '../services/NotificationService'
import UserAvatar from './UserAvatar'
import { IconBell } from './Icons'

function EmployeeHeader() {

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
        const interval = setInterval(loadNotifications, 30000)
        return () => clearInterval(interval)
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

    // Only count REQUESTS-tab notifications for the "My Requests" badge
    const requestsUnreadCount = notifications.filter(
        (n) => n.targetTab === 'REQUESTS'
    ).length

    const totalUnreadCount = notifications.length

    return (
        <header className="calendar-header">

            <Link to="/employee/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/employee/calendar">My Calendar</Link>
                <Link to="/employee/requests" className="nav-link-with-badge">
                    My Requests
                    {requestsUnreadCount > 0 && (
                        <span className="notification-badge">{requestsUnreadCount}</span>
                    )}
                </Link>
            </nav>

            <div className="header-actions">
                <div className="notification-bell-wrapper">
                    <button className="notification-bell-btn" onClick={handleBellClick}>
                        <IconBell size={20} />
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

                <UserAvatar to="/employee/profile" hasUnread={totalUnreadCount > 0} />
                <button className="logout-button" onClick={handleLogout}>
                    Logout
                </button>
            </div>

        </header>
    )
}

export default EmployeeHeader
