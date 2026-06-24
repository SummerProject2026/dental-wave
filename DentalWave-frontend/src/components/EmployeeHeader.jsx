import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { logout, getLoggedInUserId } from '../services/AuthService'
import { getUnreadCount } from '../services/NotificationService'

function EmployeeHeader() {

    const navigate = useNavigate()
    const [unreadCount, setUnreadCount] = useState(0)

    useEffect(() => {
        const userId = getLoggedInUserId()
        if (!userId) return

        getUnreadCount(userId)
            .then((response) => setUnreadCount(response.data || 0))
            .catch((err) => console.error('Failed to load notification count', err))
    }, [])

    function handleLogout() {
        logout()
        navigate('/login')
    }

    return (
        <header className="calendar-header">

            <Link to="/employee/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/employee/calendar">My Calendar</Link>
                <Link to="/employee/requests" className="nav-link-with-badge">
                    My Requests
                    {unreadCount > 0 && (
                        <span className="notification-badge">{unreadCount}</span>
                    )}
                </Link>
            </nav>

            <div className="header-actions">
                <Link to="/employee/profile" className="user-icon">
                    👤
                </Link>
                <button className="logout-button" onClick={handleLogout}>
                    Logout
                </button>
            </div>

        </header>
    )
}

export default EmployeeHeader