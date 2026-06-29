import '../App.css'
import { useState, useEffect } from 'react'
import ManagerHeader from '../components/ManagerHeader'
import {
    getAllCalendars,
    generateCalendar,
    publishCalendar,
    unpublishCalendar
} from '../services/CalendarService'
import { assignEmployeeToTeam, removeEmployeeFromTeam } from '../services/ScheduleService'
import { getEmployeesByOffice } from '../services/EmployeeService'
import { getLoggedInUserId } from '../services/AuthService'

const OFFICES = [
    { id: 1, name: 'Raleigh' },
    { id: 2, name: 'Garner' },
    { id: 3, name: 'Smithfield' }
]

function ManagerCalendarPage() {

    const today = new Date()
    const [currentDate, setCurrentDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedOfficeId, setSelectedOfficeId] = useState(OFFICES[0].id)
    const [calendars, setCalendars] = useState([])
    const [officeEmployees, setOfficeEmployees] = useState([])
    const [selectedDay, setSelectedDay] = useState(null)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const monthName = currentDate.toLocaleString('default', { month: 'long' })
    const year = currentDate.getFullYear()
    const monthLabel = `${monthName} ${year}`

    useEffect(() => {
        loadCalendars()
    }, [])

    useEffect(() => {
        getEmployeesByOffice(selectedOfficeId)
            .then((response) => setOfficeEmployees(response.data || []))
            .catch((err) => console.error('Failed to load office employees', err))
    }, [selectedOfficeId])

    function loadCalendars() {
        getAllCalendars()
            .then((response) => setCalendars(response.data || []))
            .catch((err) => console.error('Failed to load calendars', err))
    }

    function prevMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
        setSelectedDay(null)
    }

    function nextMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
        setSelectedDay(null)
    }

    const activeCalendar = calendars.find(
        (cal) => cal.officeId === selectedOfficeId && cal.month === monthLabel
    )

    function getStartAndEndDates() {
        const start = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
        const end = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0)

        const format = (d) => {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            return `${y}-${m}-${day}`
        }

        return { startDate: format(start), endDate: format(end) }
    }

    async function loadCalendarsAndReturn() {
        const response = await getAllCalendars()
        setCalendars(response.data || [])
        return response.data || []
    }

    async function handleNewCalendar() {
        if (activeCalendar) {
            setError('A calendar already exists for this office and month.')
            return
        }

        setLoading(true)
        setError('')

        try {
            const { startDate, endDate } = getStartAndEndDates()
            const createdById = getLoggedInUserId()

            await generateCalendar({
                month: monthLabel,
                startCalendarDate: startDate,
                endCalendarDate: endDate,
                createdById,
                officeId: selectedOfficeId
            })

            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to generate calendar', err)
            setError('Failed to generate a new calendar. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    async function handlePublish() {
        if (!activeCalendar) {
            setError('Generate a calendar first before publishing.')
            return
        }

        setLoading(true)
        setError('')
        try {
            await publishCalendar(activeCalendar.id)
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to publish calendar', err)
            setError('Failed to publish calendar.')
        } finally {
            setLoading(false)
        }
    }

    async function handleSaveDraft() {
        if (!activeCalendar) return

        setLoading(true)
        setError('')
        try {
            if (activeCalendar.published) {
                await unpublishCalendar(activeCalendar.id)
                await loadCalendarsAndReturn()
            }
        } catch (err) {
            console.error('Failed to save as draft', err)
            setError('Failed to save as draft.')
        } finally {
            setLoading(false)
        }
    }

    async function handleAddEmployeeToTeam(scheduleId, teamId, employeeId) {
        if (!employeeId) return

        setLoading(true)
        setError('')
        try {
            await assignEmployeeToTeam(scheduleId, teamId, employeeId)
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to add employee to team', err)
            setError('Failed to add employee to team.')
        } finally {
            setLoading(false)
        }
    }

    async function handleRemoveEmployeeFromTeam(scheduleId, teamId, employeeId) {
        setLoading(true)
        setError('')
        try {
            await removeEmployeeFromTeam(scheduleId, teamId, employeeId)
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to remove employee from team', err)
            setError('Failed to remove employee from team.')
        } finally {
            setLoading(false)
        }
    }

    const firstDayOfWeek = currentDate.getDay()
    const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate()

    const cells = []
    for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    const scheduleByDay = {}
    if (activeCalendar) {
        ;(activeCalendar.schedules || []).forEach((schedule) => {
            if (!schedule.date) return
            const day = new Date(schedule.date).getDate()
            scheduleByDay[day] = schedule
        })
    }

    const selectedSchedule = selectedDay ? scheduleByDay[selectedDay] : null

    return (
        <div className="calendar-page">

            <ManagerHeader />

            <main className="build-page-layout">

                <button
                    className="build-new-calendar-label-btn"
                    onClick={handleNewCalendar}
                    disabled={loading}
                >
                    + New calendar
                </button>

                <div className="build-page-content">

                    <aside className="build-sidebar-standalone">
                        <h3 className="manager-location-title">Location ▾</h3>
                        <ul className="manager-location-list">
                            {OFFICES.map(office => (
                                <li
                                    key={office.id}
                                    className={`manager-location-item ${selectedOfficeId === office.id ? 'active' : ''}`}
                                    onClick={() => {
                                        setSelectedOfficeId(office.id)
                                        setSelectedDay(null)
                                    }}
                                >
                                    {office.name}
                                </li>
                            ))}
                        </ul>
                    </aside>

                    <div className="build-mini-calendar-card">

                        <div className="mini-calendar-controls">
                            <button onClick={prevMonth}>&lt;</button>
                            <span>{monthName}</span>
                            <span>{year}</span>
                            <button onClick={nextMonth}>&gt;</button>
                        </div>

                        {activeCalendar?.published && (
                            <p className="calendar-published-badge">Published</p>
                        )}

                        {error && <p className="error-message">{error}</p>}

                        <div className="mini-calendar-grid">
                            {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(d => (
                                <div key={d} className="mini-cal-header">{d}</div>
                            ))}
                            {cells.map((day, i) => (
                                <div
                                    key={i}
                                    className={[
                                        'mini-cal-day',
                                        !day ? 'empty' : '',
                                        scheduleByDay[day] ? 'calendar-day-has-schedule' : '',
                                        selectedDay === day ? 'mini-cal-day-selected' : ''
                                    ].join(' ')}
                                    onClick={() => day && scheduleByDay[day] && setSelectedDay(day)}
                                >
                                    {day || ''}
                                </div>
                            ))}
                        </div>

                    </div>

                </div>

                {selectedSchedule && (
                    <div className="day-teams-panel">
                        <h3>Teams for {monthName} {selectedDay}, {year}</h3>

                        {selectedSchedule.teams && Object.keys(selectedSchedule.teams).length > 0 ? (
                            Object.entries(selectedSchedule.teams).map(([teamId, employees]) => {
                                const assignedIds = employees.map((e) => e.id)
                                const availableToAdd = officeEmployees.filter(
                                    (e) => !assignedIds.includes(e.id)
                                )

                                return (
                                    <div key={teamId} className="day-team-block">
                                        <strong>Team {teamId}</strong>
                                        <ul>
                                            {employees.map((emp) => (
                                                <li key={emp.id}>
                                                    {emp.firstName} {emp.lastName} ({emp.position})
                                                    <button
                                                        className="team-remove-btn"
                                                        onClick={() => handleRemoveEmployeeFromTeam(
                                                            selectedSchedule.id, teamId, emp.id
                                                        )}
                                                        disabled={loading}
                                                    >
                                                        ✕
                                                    </button>
                                                </li>
                                            ))}
                                        </ul>

                                        <select
                                            className="team-add-select"
                                            value=""
                                            onChange={(e) => handleAddEmployeeToTeam(
                                                selectedSchedule.id, teamId, Number(e.target.value)
                                            )}
                                            disabled={loading}
                                        >
                                            <option value="">+ Add employee...</option>
                                            {availableToAdd.map((emp) => (
                                                <option key={emp.id} value={emp.id}>
                                                    {emp.firstName} {emp.lastName} ({emp.position})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                )
                            })
                        ) : (
                            <p>No team assignments for this day.</p>
                        )}
                    </div>
                )}

                <div className="build-page-actions">
                    <button className="save-draft-btn" onClick={handleSaveDraft} disabled={loading}>
                        Save as Draft
                    </button>
                    <button className="publish-btn" onClick={handlePublish} disabled={loading}>
                        Publish
                    </button>
                </div>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerCalendarPage