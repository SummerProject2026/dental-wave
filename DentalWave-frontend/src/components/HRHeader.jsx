import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { logout, getLoggedInUserId } from '../services/AuthService'
import { getUnreadNotifications } from '../services/NotificationService'
import UserAvatar from './UserAvatar'

function HRHeader() {

    const navigate = useNavigate()
    const [hasUnread, setHasUnread] = useState(false)

    useEffect(() => {
        const userId = getLoggedInUserId()
        if (!userId) return

        getUnreadNotifications(userId)
            .then((response) => setHasUnread((response.data || []).length > 0))
            .catch((err) => console.error('Failed to load notifications', err))
    }, [])

    function handleLogout() {
        logout()
        navigate('/login')
    }

    return (
        <header className="calendar-header">

            <Link to="/hr/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/hr/calendar">Calendar</Link>
                <Link to="/hr/requests">Requests</Link>
                <Link to="/hr/employees">Employees</Link>
            </nav>

            <div className="header-actions">
                <UserAvatar to="/hr/profile" hasUnread={hasUnread} />

                <button
                    className="logout-button"
                    onClick={handleLogout}
                >
                    Logout
                </button>
            </div>

        </header>
    )
}

export default HRHeader