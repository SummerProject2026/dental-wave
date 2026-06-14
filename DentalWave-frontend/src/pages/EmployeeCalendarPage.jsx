import '../App.css'
import EmployeeHeader from '../components/EmployeeHeader'
import { useState } from 'react'

// Placeholder schedule data - replace with API call later
const mockSchedule = {
    '2026-6-14': {
        time: '8:00 am - 5:00 pm',
        office: 'Raleigh',
        notes: 'Doctor assigned'
    },
    '2026-6-16': {
        time: '9:00 am - 3:00 pm',
        office: 'Garner',
        notes: 'Team A'
    }
}

function EmployeeCalendarPage() {

    const today = new Date()
    const [currentDate, setCurrentDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedDay, setSelectedDay] = useState(null)

    function prevMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
        setSelectedDay(null)
    }

    function nextMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
        setSelectedDay(null)
    }

    const monthName = currentDate.toLocaleString('default', { month: 'long' })
    const year = currentDate.getFullYear()
    const month = currentDate.getMonth() + 1

    const firstDayOfWeek = currentDate.getDay()
    const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate()

    const cells = []
    for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    const isToday = (day) =>
        day === today.getDate() &&
        currentDate.getMonth() === today.getMonth() &&
        currentDate.getFullYear() === today.getFullYear()

    function handleDayClick(day) {
        if (!day) return
        setSelectedDay(day)
    }

    function getScheduleKey(day) {
        return `${year}-${month}-${day}`
    }

    const selectedSchedule = selectedDay ? mockSchedule[getScheduleKey(selectedDay)] : null

    const selectedDateLabel = selectedDay
        ? new Date(year, month - 1, selectedDay).toLocaleDateString('en-US', {
            month: 'short', day: 'numeric', year: 'numeric'
        })
        : null

    return (
        <div className="calendar-page">

            <EmployeeHeader />

            <main className="employee-calendar-layout">

                {/* Left panel — shows when a day is selected */}
                <aside className="employee-day-panel">
                    {selectedDay ? (
                        <>
                            <h2 className="employee-day-title">{selectedDateLabel}</h2>

                            <div className="employee-day-card">
                                {selectedSchedule ? (
                                    <>
                                        <p><strong>Time:</strong> {selectedSchedule.time}</p>
                                        <p><strong>Office:</strong> {selectedSchedule.office}</p>
                                        <p><strong>Notes:</strong> {selectedSchedule.notes}</p>
                                    </>
                                ) : (
                                    <p className="no-schedule-text">No Schedule Published</p>
                                )}
                            </div>

                            <button className="request-timeoff-btn" onClick={() => navigate('/employee/requests/new')}>
                                Request Time Off
                            </button>
                        </>
                    ) : (
                        <p className="employee-day-hint">Click a day to view your schedule</p>
                    )}
                </aside>

                {/* Right panel — calendar */}
                <div className="employee-calendar-section">

                    <div className="calendar-controls">
                        <button onClick={prevMonth}>&lt;</button>
                        <h2>{monthName} {year}</h2>
                        <button onClick={nextMonth}>&gt;</button>
                    </div>

                    <div className="calendar-grid">
                        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                            <div key={d} className="calendar-day-header">{d}</div>
                        ))}
                        {cells.map((day, i) => (
                            <div
                                key={i}
                                onClick={() => handleDayClick(day)}
                                className={[
                                    'calendar-day',
                                    isToday(day) ? 'calendar-day-today' : '',
                                    !day ? 'calendar-day-empty' : '',
                                    day && selectedDay === day ? 'calendar-day-selected' : '',
                                    day && mockSchedule[getScheduleKey(day)] ? 'calendar-day-scheduled' : ''
                                ].join(' ')}
                            >
                                {day || ''}
                            </div>
                        ))}
                    </div>

                </div>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>

    )
}

export default EmployeeCalendarPage