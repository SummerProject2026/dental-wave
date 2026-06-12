import { Link, useNavigate } from 'react-router-dom'
import { logout } from '../services/AuthService'

function HRHeader() {

    const navigate = useNavigate()

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
                <Link to="/hr/profile" className="user-icon">
                    👤
                </Link>

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