import '../App.css'
import EmployeeHeader from '../components/EmployeeHeader'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { getPublishedCalendars } from '../services/CalendarService'
import { getAllOffices } from '../services/OfficeService'

function parseLocalDate(dateValue) {
    if (!dateValue) return null
    const [datePart] = String(dateValue).split('T')
    const [year, month, day] = datePart.split('-').map(Number)
    if (!year || !month || !day) return null
    return new Date(year, month - 1, day)
}

function getEmployeeName(employee) {
    return `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
}

function EmployeeCalendarPage() {

    const today = new Date()
    const [currentDate, setCurrentDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedDay, setSelectedDay] = useState(null)
    const [publishedCalendars, setPublishedCalendars] = useState([])
    const [offices, setOffices] = useState([])
    const navigate = useNavigate()

    useEffect(() => {
        getPublishedCalendars()
            .then((response) => setPublishedCalendars(response.data || []))
            .catch((err) => console.error('Failed to load published calendars', err))

        getAllOffices()
            .then((response) => setOffices(response.data || []))
            .catch((err) => console.error('Failed to load offices', err))
    }, [])

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
    const monthLabel = `${monthName} ${year}`

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

    const officeNameById = offices.reduce((names, office) => {
        names[office.id] = office.name
        return names
    }, {})

    const monthCalendars = publishedCalendars.filter((calendar) => calendar.month === monthLabel)
    const calendarStatus = monthCalendars.length > 0
        ? 'Published'
        : 'Not Created'

    function getSchedulesForDay(day) {
        if (!day) return []

        return monthCalendars.flatMap((calendar) =>
            (calendar.schedules || [])
                .filter((schedule) => parseLocalDate(schedule.date)?.getDate() === day)
                .map((schedule) => ({
                    schedule,
                    officeName: officeNameById[calendar.officeId] || calendar.officeName || 'Office'
                }))
        )
    }

    function getTeamEntries(schedule) {
        return Object.entries(schedule.teams || {}).map(([teamId, employees]) => ({
            id: teamId,
            name: schedule.teamNames?.[Number(teamId)] || `Team ${teamId}`,
            employees: employees || []
        }))
    }

    function handleDayClick(day) {
        if (!day) return
        setSelectedDay(day)
    }

    const selectedDateLabel = selectedDay
        ? new Date(year, currentDate.getMonth(), selectedDay).toLocaleDateString('en-US', {
            weekday: 'long',
            month: 'long',
            day: 'numeric'
        })
        : null

    const selectedDaySchedules = selectedDay ? getSchedulesForDay(selectedDay) : []

    return (
        <div className="calendar-page">

            <EmployeeHeader />

            <main className="employee-calendar-layout">

                <aside className="universal-day-panel employee-universal-day-panel">
                    {selectedDay ? (
                        <>
                            <h2>{selectedDateLabel}</h2>
                            <p className="universal-day-panel-month">{monthName} {year}</p>

                            {selectedDaySchedules.length > 0 ? (
                                selectedDaySchedules.map(({ schedule, officeName }) => (
                                    <section key={schedule.id} className="universal-location-card">
                                        <h3>{officeName}</h3>
                                        {getTeamEntries(schedule).length > 0 ? (
                                            getTeamEntries(schedule).map((team) => (
                                                <div key={team.id} className="universal-team-block">
                                                    <h4>{team.name}</h4>
                                                    {team.employees.length > 0 ? (
                                                        <ul>
                                                            {team.employees.map((employee) => (
                                                                <li key={employee.id}>
                                                                    <span>{getEmployeeName(employee)}</span>
                                                                    <small>{employee.position || 'Assistant'}</small>
                                                                </li>
                                                            ))}
                                                        </ul>
                                                    ) : (
                                                        <p>No assistants assigned</p>
                                                    )}
                                                </div>
                                            ))
                                        ) : (
                                            <p>No teams assigned</p>
                                        )}
                                        {schedule.notes && (
                                            <p className="universal-schedule-notes">{schedule.notes}</p>
                                        )}
                                    </section>
                                ))
                            ) : (
                                <p className="universal-empty-day">No published teams for this day.</p>
                            )}

                            <button className="request-timeoff-btn" onClick={() => navigate('/employee/requests/new')}>
                                Request Time Off
                            </button>
                        </>
                    ) : (
                        <p className="universal-day-hint">Select a day to view the published schedule.</p>
                    )}
                </aside>

                <div className="employee-calendar-section universal-calendar-main">

                    <div className="universal-calendar-heading">
                        <h1>Universal Calendar</h1>
                        <span className={`universal-status-badge ${calendarStatus.toLowerCase().replaceAll(' ', '-')}`}>
                            {calendarStatus}
                        </span>
                    </div>

                    {monthCalendars.length === 0 && (
                        <p className="universal-calendar-empty-message">
                            No published schedule for {monthName} {year} yet.
                        </p>
                    )}

                    <div className="calendar-controls">
                        <button onClick={prevMonth}>&lt;</button>
                        <h2>{monthName} {year}</h2>
                        <button onClick={nextMonth}>&gt;</button>
                    </div>

                    <div className="calendar-grid universal-calendar-grid">
                        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                            <div key={d} className="calendar-day-header">{d}</div>
                        ))}
                        {cells.map((day, i) => {
                            const daySchedules = getSchedulesForDay(day)
                            const teamCount = daySchedules.reduce((count, { schedule }) => (
                                count + getTeamEntries(schedule).length
                            ), 0)

                            return (
                                <div
                                    key={i}
                                    onClick={() => handleDayClick(day)}
                                    className={[
                                        'calendar-day',
                                        isToday(day) ? 'calendar-day-today' : '',
                                        !day ? 'calendar-day-empty' : '',
                                        day && selectedDay === day ? 'calendar-day-selected' : '',
                                        daySchedules.length > 0 ? 'calendar-day-has-published-schedule' : ''
                                    ].join(' ')}
                                >
                                    <span className="universal-calendar-day-number">{day || ''}</span>
                                    {daySchedules.length > 0 && (
                                        <div className="universal-calendar-summary">
                                            {daySchedules.map(({ schedule, officeName }) => (
                                                <div key={schedule.id} className="universal-calendar-office">
                                                    <strong>{officeName}</strong>
                                                    <span>{getTeamEntries(schedule).length} team{getTeamEntries(schedule).length === 1 ? '' : 's'}</span>
                                                </div>
                                            ))}
                                            <span className="universal-calendar-total">{teamCount} total team{teamCount === 1 ? '' : 's'}</span>
                                        </div>
                                    )}
                                </div>
                            )
                        })}
                    </div>

                </div>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>

    )
}

export default EmployeeCalendarPage
