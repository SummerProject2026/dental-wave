import '../App.css'
import HRHeader from '../components/HRHeader'

function HRCalendarPage() {
    return (
        <div className="calendar-page">

            <HRHeader />

            <main className="calendar-content">

                <h1>Universal Calendar</h1>

                <div className="calendar-controls">
                    <button>&lt;</button>
                    <h2>June 2026</h2>
                    <button>&gt;</button>
                </div>

                <div className="calendar-grid">

                    <div className="calendar-day-header">Sun</div>
                    <div className="calendar-day-header">Mon</div>
                    <div className="calendar-day-header">Tue</div>
                    <div className="calendar-day-header">Wed</div>
                    <div className="calendar-day-header">Thu</div>
                    <div className="calendar-day-header">Fri</div>
                    <div className="calendar-day-header">Sat</div>

                    {Array.from({ length: 35 }, (_, index) => (
                        <div
                            key={index}
                            className="calendar-day"
                        >
                            {index + 1 <= 30 ? index + 1 : ''}
                        </div>
                    ))}

                </div>

            </main>

            <footer className="page-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default HRCalendarPage