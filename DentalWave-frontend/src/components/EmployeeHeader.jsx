import { Link } from 'react-router-dom'

function EmployeeHeader() {
    return (
        <header className="calendar-header">
            <Link to="/employee/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/employee/calendar">My Calendar</Link>
                <Link to="/employee/requests">My Requests</Link>
            </nav>

            <Link to="/employee/profile" className="user-icon">
                👤
            </Link>
        </header>
    )
}

export default EmployeeHeader