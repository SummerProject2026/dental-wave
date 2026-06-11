import '../App.css'
import logo from '../pictures/wake-logo.png'
import EmployeeHeader from "../components/EmployeeHeader.jsx";

/**
 * Manager Calendar Page
 *
 * This is the main page a Manager sees after logging in.
 * For Sprint 1, this page uses placeholder calendar data.
 */
function ManagerCalendarPage() {

    const currentMonth = new Date().toLocaleString('default', {
        month: 'long',
        year: 'numeric'
    })

    const cells = Array.from({ length: 24 })

    return (
        <div className="calendar-page">

            <EmployeeHeader />

            <main className="calendar-layout">

                <aside className="calendar-sidebar">
                    <img
                        src={logo}
                        alt="Wake Orthodontics"
                        className="calendar-logo"
                    />

                    <h2>{currentMonth}</h2>
                </aside>


            </main>

            <footer className="calendar-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default ManagerCalendarPage