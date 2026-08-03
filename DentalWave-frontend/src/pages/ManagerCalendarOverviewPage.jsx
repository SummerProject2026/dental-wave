import '../App.css'
import { useEffect, useState } from 'react'
import ManagerHeader from '../components/ManagerHeader'
import { getAllCalendars, getPublishedCalendars } from '../services/CalendarService'
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

function ManagerCalendarOverviewPage({ printMode = false }) {

    const today = new Date()
    const [currentDate, setCurrentDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedLocation, setSelectedLocation] = useState('Universal')
    const [selectedDay, setSelectedDay] = useState(null)
    const [publishedCalendars, setPublishedCalendars] = useState([])
    const [allCalendars, setAllCalendars] = useState([])
    const [offices, setOffices] = useState([])

    useEffect(() => {
        getPublishedCalendars()
            .then((response) => setPublishedCalendars(response.data || []))
            .catch((err) => console.error('Failed to load published calendars', err))

        getAllCalendars()
            .then((response) => setAllCalendars(response.data || []))
            .catch((err) => console.error('Failed to load calendar status', err))

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

    const locations = ['Universal', ...offices.map((office) => office.name)]
    const officeNameById = offices.reduce((names, office) => {
        names[office.id] = office.name
        return names
    }, {})

    const monthCalendars = publishedCalendars.filter((calendar) => calendar.month === monthLabel)
    const monthDraftCalendars = allCalendars.filter((calendar) => calendar.month === monthLabel)
    const calendarStatus = monthCalendars.length > 0
        ? 'Published'
        : monthDraftCalendars.length > 0
            ? 'Draft'
            : 'Not Created'

    function getSchedulesForDay(day) {
        if (!day) return []

        return monthCalendars.flatMap((calendar) => {
            const officeName = officeNameById[calendar.officeId] || calendar.officeName || 'Office'
            if (selectedLocation !== 'Universal' && officeName !== selectedLocation) return []

            return (calendar.schedules || [])
                .filter((schedule) => parseLocalDate(schedule.date)?.getDate() === day)
                .map((schedule) => ({ schedule, officeName }))
        })
    }

    function handleLocationChange(location) {
        setSelectedLocation(location)
        setSelectedDay(null)
    }

    function handleDayClick(day) {
        if (!day) return
        setSelectedDay(day)
    }

    function getTeamEntries(schedule) {
        return Object.entries(schedule.teams || {}).map(([teamId, employees]) => ({
            id: teamId,
            name: schedule.teamNames?.[Number(teamId)] || `Team ${teamId}`,
            employees: employees || []
        }))
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

            <ManagerHeader />

            <main className="manager-calendar-layout">
                {printMode && <button className="lite-button print-trigger" onClick={() => window.print()}>Print landscape schedule</button>}

                <aside className="manager-sidebar">
                    <div className="manager-location-section">
                        <h3 className="manager-location-title">Location ▾</h3>
                        <ul className="manager-location-list">
                            {locations.map(loc => (
                                <li
                                    key={loc}
                                    className={`manager-location-item ${selectedLocation === loc ? 'active' : ''}`}
                                    onClick={() => handleLocationChange(loc)}
                                >
                                    {loc}
                                </li>
                            ))}
                        </ul>
                    </div>
                </aside>

                <div className="manager-calendar-section universal-calendar-main">

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

                <aside className="universal-day-panel">
                    {selectedDay ? (
                        <>
                            <h2>{selectedDateLabel}</h2>
                            <p className="universal-day-panel-month">
                                {selectedLocation === 'Universal' ? 'All locations' : selectedLocation}
                            </p>

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
                        </>
                    ) : (
                        <p className="universal-day-hint">Select a day to view teams by location.</p>
                    )}
                </aside>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerCalendarOverviewPage
