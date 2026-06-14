import '../App.css'
import { useState } from 'react'
import HRHeader from '../components/HRHeader'

function HRCalendarPage() {

    const today = new Date()
    const [currentDate, setCurrentDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))

    function prevMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
    }

    function nextMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
    }

    const monthName = currentDate.toLocaleString('default', { month: 'long' })
    const year = currentDate.getFullYear()

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

    return (
        <div className="calendar-page">

            <HRHeader />

            <main className="calendar-content">

                <h1 className="calendar-content h1">Universal Calendar</h1>

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
                            className={[
                                'calendar-day',
                                isToday(day) ? 'calendar-day-today' : '',
                                !day ? 'calendar-day-empty' : ''
                            ].join(' ')}
                        >
                            {day || ''}
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