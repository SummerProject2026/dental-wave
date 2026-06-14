import { Link, useNavigate } from 'react-router-dom'
import { logout } from '../services/AuthService'

function EmployeeHeader() {

    const navigate = useNavigate()

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
                <Link to="/employee/requests">My Requests</Link>
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