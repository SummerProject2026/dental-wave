import { Link, useNavigate } from 'react-router'
import { logout } from '../services/AuthService'

function ManagerHeader() {
    const navigate = useNavigate()
    function handleLogout() {
        logout()
        navigate('/login')
    }

    return (
        <header className="calendar-header">
            <Link to="/manager/dashboard" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/manager/dashboard">Dashboard</Link>
                <Link to="/manager/schedule">Monthly Schedule</Link>
                <Link to="/manager/doctors">Doctors</Link>
                <Link to="/manager/assistants">Assistants</Link>
            </nav>

            <div className="header-actions">

                <button className="logout-button" onClick={handleLogout}>Logout</button>
            </div>
        </header>
    )
}

export default ManagerHeader
