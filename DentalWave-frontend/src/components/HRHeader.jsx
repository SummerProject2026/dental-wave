import { Link } from 'react-router-dom'

function HRHeader() {
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

            <Link to="/hr/profile" className="user-icon">
                👤
            </Link>

        </header>
    )
}

export default HRHeader