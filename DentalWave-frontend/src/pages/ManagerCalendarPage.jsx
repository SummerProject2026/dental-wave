import '../App.css'
import logo from '../pictures/wake-logo.png'

/**
 * Manager Calendar Page
 *
 * This is the main page a Manager sees after logging in.
 * For Sprint 1, this page uses placeholder calendar data.
 */
function ManagerCalendarPage() {

    const days = [
        1, 2, 3, 4,
        8, 9, 10, 11,
        15, 16, 17, 18,
        22, 23, 24, 25,
        29, 30, '', ''
    ]

    return (
        <div className="calendar-page">

            {/* Top navigation/header */}
            <div className="main-nav">
                <h2>Wake Orthodontics and Pediatric Dentistry</h2>

                <div className="nav-links">
                    <span>Calendar</span>
                    <span>Approved Request</span>
                    <span>Employees</span>
                </div>

                <span className="user-icon">👤</span>
            </div>

            <div className="calendar-layout">

                {/* Left sidebar */}
                <div className="calendar-sidebar">
                    <img
                        className="sidebar-logo"
                        src={logo}
                        alt="Wake Orthodontics and Pediatric Dentistry"
                    />

                    <h2 className="location-title">Location⌄</h2>

                    <div className="location-list">
                        <p className="selected-location">Universal</p>
                        <p>Raleigh</p>
                        <p>Garner</p>
                        <p>Smithfield</p>
                    </div>
                </div>

                {/* Main calendar area */}
                <div className="calendar-main">
                    <h1 className="calendar-title">June 2026</h1>

                    <div className="calendar-grid">
                        {days.map((day, index) => (
                            <div className="calendar-cell" key={index}>
                                <span className="day-number">
                                    {day && `[${day}]`}
                                </span>

                                {index === 0 && (
                                    <div className="shift-text">
                                        <p>M) Cori* Madison Jordan Logan Sara</p>
                                        <p>C) Jackie* Constance Alyssa Rachel Jenna</p>
                                        <p>L) Diana* Carson Tanya Jenna</p>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

            </div>

            <p className="calendar-footer">© All Rights Reserved</p>
        </div>
    )
}

export default ManagerCalendarPage