import '../App.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import ManagerHeader from '../components/ManagerHeader'
import { createCalendar } from '../services/CalendarService'
import { getLoggedInUserId } from '../services/AuthService'

// Office name → id mapping. Matches the offices already seeded in the database.
const OFFICES = [
    { id: 1, name: 'Raleigh' },
    { id: 2, name: 'Garner' },
    { id: 3, name: 'Smithfield' }
]

function ManagerNewCalendarPage() {
    const navigate = useNavigate()

    const today = new Date()
    const [selectedMonth, setSelectedMonth] = useState(today.getMonth())
    const [selectedYear, setSelectedYear] = useState(today.getFullYear())
    const [selectedOfficeId, setSelectedOfficeId] = useState(OFFICES[0].id)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ]

    function getMonthLabel() {
        return `${monthNames[selectedMonth]} ${selectedYear}`
    }

    function getStartAndEndDates() {
        const start = new Date(selectedYear, selectedMonth, 1)
        const end = new Date(selectedYear, selectedMonth + 1, 0)

        const format = (d) => {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            return `${y}-${m}-${day}`
        }

        return { startDate: format(start), endDate: format(end) }
    }

    async function handleSave(published) {
        setError('')
        setLoading(true)

        const { startDate, endDate } = getStartAndEndDates()
        const createdById = getLoggedInUserId()

        const calendarToCreate = {
            month: getMonthLabel(),
            startCalendarDate: startDate,
            endCalendarDate: endDate,
            published,
            createdById,
            officeId: selectedOfficeId
        }

        try {
            const response = await createCalendar(calendarToCreate)
            navigate(`/manager/calendar/${response.data.id}/edit`)
        } catch (err) {
            console.error('Failed to create calendar', err)
            setError('Failed to create calendar. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="calendar-page">

            <ManagerHeader />

            <main className="manager-new-calendar-content">

                <div className="manager-new-calendar-card">

                    <div className="new-calendar-top-row">
                        <span></span>
                        <button className="add-calendar-link" disabled>
                            + New calendar
                        </button>
                    </div>

                    <div className="new-calendar-body">

                        <div className="new-calendar-location">
                            <label>Location ▾</label>
                            <select
                                value={selectedOfficeId}
                                onChange={(e) => setSelectedOfficeId(Number(e.target.value))}
                            >
                                {OFFICES.map((office) => (
                                    <option key={office.id} value={office.id}>
                                        {office.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="new-calendar-date-picker">
                            <select
                                value={selectedMonth}
                                onChange={(e) => setSelectedMonth(Number(e.target.value))}
                            >
                                {monthNames.map((name, index) => (
                                    <option key={name} value={index}>{name}</option>
                                ))}
                            </select>

                            <select
                                value={selectedYear}
                                onChange={(e) => setSelectedYear(Number(e.target.value))}
                            >
                                {[today.getFullYear() - 1, today.getFullYear(), today.getFullYear() + 1].map((y) => (
                                    <option key={y} value={y}>{y}</option>
                                ))}
                            </select>
                        </div>

                        {error && <p className="error-message">{error}</p>}

                    </div>

                    <div className="new-calendar-actions">
                        <button
                            className="save-draft-btn"
                            onClick={() => handleSave(false)}
                            disabled={loading}
                        >
                            Save as Draft
                        </button>
                        <button
                            className="publish-btn"
                            onClick={() => handleSave(true)}
                            disabled={loading}
                        >
                            Publish
                        </button>
                    </div>

                </div>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerNewCalendarPage