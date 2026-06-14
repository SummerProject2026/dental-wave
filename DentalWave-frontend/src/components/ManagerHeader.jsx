import { Link, useNavigate } from 'react-router-dom'
import { logout } from '../services/AuthService'

function ManagerHeader() {
    const navigate = useNavigate()

    function handleLogout() {
        logout()
        navigate('/login')
    }

    return (
        <header className="calendar-header">
            <Link to="/manager/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/manager/calendar">Calendar</Link>
                <Link to="/manager/requests">Approved Request</Link>
                <Link to="/manager/employees">Employees</Link>
            </nav>

            <div className="header-actions">
                <Link to="/manager/profile" className="user-icon">👤</Link>
                <button className="logout-button" onClick={handleLogout}>Logout</button>
            </div>
        </header>
    )
}

export default ManagerHeader