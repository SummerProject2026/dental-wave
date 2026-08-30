import '../App.css'
import { useState, useEffect } from 'react'
import { useParams, useSearchParams } from 'react-router'
import ManagerHeader from '../components/ManagerHeader'
import {
    getAllCalendars,
    createCalendar,
    publishCalendar,
    unpublishCalendar,
    addScheduleToCalendar
} from '../services/CalendarService'
import { getLoggedInUserId } from '../services/AuthService'
import { getAllOffices } from '../services/OfficeService'

const OFFICES = [
    { id: 1, name: 'Raleigh' },
    { id: 2, name: 'Garner' },
    { id: 3, name: 'Smithfield' }
]

function parseLocalDate(dateValue) {
    if (!dateValue) return null

    const [datePart] = String(dateValue).split('T')
    const [year, month, day] = datePart.split('-').map(Number)

    if (!year || !month || !day) return null
    return new Date(year, month - 1, day)
}

function ManagerEditCalendarPage() {
    const { id } = useParams()
    const [searchParams] = useSearchParams()

    const today = new Date()
    const [currentDate, setCurrentDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [offices, setOffices] = useState(OFFICES)
    const [selectedOfficeId, setSelectedOfficeId] = useState(
        Number(searchParams.get('officeId')) || ''
    )
    const [calendars, setCalendars] = useState([])
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const monthName = currentDate.toLocaleString('default', { month: 'long' })
    const year = currentDate.getFullYear()
    const monthLabel = `${monthName} ${year}`

    function loadOffices() {
        getAllOffices()
            .then((response) => {
                const loadedOffices = response.data || []
                if (loadedOffices.length === 0) return

                setOffices(loadedOffices)
                setSelectedOfficeId((currentOfficeId) => currentOfficeId || loadedOffices[0].id)
            })
            .catch((err) => {
                console.error('Failed to load offices', err)
                setSelectedOfficeId((currentOfficeId) => currentOfficeId || OFFICES[0].id)
            })
    }

    function loadCalendars() {
        getAllCalendars()
            .then((response) => {
                const loadedCalendars = response.data || []
                const routeCalendar = id
                    ? loadedCalendars.find((cal) => cal.id === Number(id))
                    : null

                if (routeCalendar) {
                    const startDate = parseLocalDate(routeCalendar.startCalendarDate)
                    if (startDate) {
                        setCurrentDate(new Date(startDate.getFullYear(), startDate.getMonth(), 1))
                    }
                    setSelectedOfficeId(routeCalendar.officeId)
                }

                setCalendars(loadedCalendars)
            })
            .catch((err) => console.error('Failed to load calendars', err))
    }

    useEffect(() => {
        loadOffices()
        loadCalendars()
    }, [])

    function prevMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
    }

    function nextMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
    }

    const activeCalendar = id
        ? calendars.find((cal) => cal.id === Number(id))
        : calendars.find((cal) => cal.officeId === selectedOfficeId && cal.month === monthLabel)

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

    async function ensureCalendarExists() {
        if (activeCalendar) return activeCalendar
        if (!selectedOfficeId) throw new Error('Select an office before creating a calendar.')

        const { startDate, endDate } = getStartAndEndDates()
        const createdById = getLoggedInUserId()

        const response = await createCalendar({
            month: monthLabel,
            startCalendarDate: startDate,
            endCalendarDate: endDate,
            published: false,
            createdById,
            officeId: selectedOfficeId
        })

        const refreshed = await loadCalendarsAndReturn()
        return refreshed.find((cal) => cal.id === response.data.id) || response.data
    }

    async function loadCalendarsAndReturn() {
        const response = await getAllCalendars()
        setCalendars(response.data || [])
        return response.data || []
    }

    function isSunday(day) {
        if (!day) return false
        return new Date(currentDate.getFullYear(), currentDate.getMonth(), day).getDay() === 0
    }

    async function handleDayClick(day) {
        if (!day) return
        if (isSunday(day)) return

        setLoading(true)
        setError('')

        try {
            const calendar = await ensureCalendarExists()

            const existingSchedule = (calendar.schedules || []).find((s) => {
                if (!s.date) return false
                const scheduleDate = parseLocalDate(s.date)
                return scheduleDate?.getDate() === day
            })

            if (existingSchedule) {
                // Schedule already exists for this day — Pass 2 will open the team panel here
                return
            }

            const dateStr = `${year}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`

            await addScheduleToCalendar(calendar.id, {
                date: dateStr,
                startTime: '08:00:00',
                endTime: '17:00:00',
                published: false
            })

            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to build schedule for day', err)
            setError('Failed to update the schedule for this day.')
        } finally {
            setLoading(false)
        }
    }

    async function handlePublish() {
        setLoading(true)
        setError('')
        try {
            const calendar = await ensureCalendarExists()
            await publishCalendar(calendar.id)
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to publish calendar', err)
            setError('Failed to publish calendar.')
        } finally {
            setLoading(false)
        }
    }

    async function handleSaveDraft() {
        setLoading(true)
        setError('')
        try {
            const calendar = await ensureCalendarExists()
            if (calendar.published) {
                await unpublishCalendar(calendar.id)
                await loadCalendarsAndReturn()
            }
        } catch (err) {
            console.error('Failed to save as draft', err)
            setError('Failed to save as draft.')
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
            const scheduleDate = parseLocalDate(schedule.date)
            if (!scheduleDate) return
            const day = scheduleDate.getDate()
            scheduleByDay[day] = schedule
        })
    }

    const isToday = (day) =>
        day === today.getDate() &&
        currentDate.getMonth() === today.getMonth() &&
        currentDate.getFullYear() === today.getFullYear()

    return (
        <div className="calendar-page">

            <ManagerHeader />

            <main className="manager-calendar-layout">

                <aside className="manager-sidebar">
                    <div className="manager-location-section">
                        <h3 className="manager-location-title">Location ▾</h3>
                        <ul className="manager-location-list">
                            {offices.map(office => (
                                <li
                                    key={office.id}
                                    className={`manager-location-item ${selectedOfficeId === office.id ? 'active' : ''}`}
                                    onClick={() => setSelectedOfficeId(office.id)}
                                >
                                    {office.name}
                                </li>
                            ))}
                        </ul>
                    </div>
                </aside>

                <div className="manager-calendar-section">

                    <div className="calendar-controls">
                        <button onClick={prevMonth}>&lt;</button>
                        <h2>{monthName} {year}</h2>
                        <button onClick={nextMonth}>&gt;</button>
                    </div>

                    {activeCalendar?.published && (
                        <p className="calendar-published-badge">Published</p>
                    )}

                    {error && <p className="error-message">{error}</p>}

                    <div className="calendar-grid">
                        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                            <div key={d} className="calendar-day-header">{d}</div>
                        ))}
                        {cells.map((day, i) => (
                            <div
                                key={i}
                                className={`calendar-day calendar-day-editable ${isToday(day) ? 'calendar-day-today' : ''} ${!day ? 'calendar-day-empty' : ''} ${scheduleByDay[day] ? 'calendar-day-has-schedule' : ''} ${isSunday(day) ? 'calendar-day-sunday' : ''}`}
                                onClick={() => handleDayClick(day)}
                            >
                                {day || ''}
                                {scheduleByDay[day] && (
                                    <div className="calendar-day-schedule-marker">●</div>
                                )}
                            </div>
                        ))}
                    </div>

                    <div className="new-calendar-actions">
                        <button className="save-draft-btn" onClick={handleSaveDraft} disabled={loading}>
                            Save as Draft
                        </button>
                        <button className="publish-btn" onClick={handlePublish} disabled={loading}>
                            Publish
                        </button>
                    </div>

                </div>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerEditCalendarPage
