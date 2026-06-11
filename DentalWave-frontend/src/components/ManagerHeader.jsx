import { Link } from 'react-router-dom'

function ManagerHeader() {
    return (
        <header className="calendar-header">

            <Link to="/manager/calendar" className="business-name">
                Wake Orthodontics and Pediatric Dentistry
            </Link>

            <nav>
                <Link to="/manager/calendar">Calendar</Link>
                <Link to="/manager/approved-requests">
                    Approved Requests
                </Link>
                <Link to="/manager/employees">Employees</Link>
            </nav>

            <Link to="/manager/profile" className="user-icon">
                👤
            </Link>

        </header>
    )
}

export default ManagerHeader