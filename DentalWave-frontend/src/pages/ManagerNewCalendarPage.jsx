import '../App.css'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import ManagerHeader from '../components/ManagerHeader'
import { generateCalendar, getAllCalendars } from '../services/CalendarService'
import { getLoggedInUserId } from '../services/AuthService'
import { getAllOffices } from '../services/OfficeService'
import { getResources, getReusableTeams } from '../services/ManagerSchedulerService'

function formatDate(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

function ManagerNewCalendarPage() {
    const navigate = useNavigate()
    const today = new Date()
    const [monthValue, setMonthValue] = useState(
        `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`
    )
    const [offices, setOffices] = useState([])
    const [counts, setCounts] = useState({ doctors: 0, assistants: 0, teams: 0 })
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const monthDate = useMemo(() => {
        const [year, month] = monthValue.split('-').map(Number)
        return new Date(year, month - 1, 1)
    }, [monthValue])
    const monthLabel = monthDate.toLocaleDateString('en-US', {
        month: 'long',
        year: 'numeric'
    })

    useEffect(() => {
        Promise.all([
            getAllOffices(),
            getResources('DOCTOR'),
            getResources('ASSISTANT'),
            getReusableTeams()
        ])
            .then(([officeResponse, doctorResponse, assistantResponse, teamResponse]) => {
                setOffices(officeResponse.data || [])
                setCounts({
                    doctors: (doctorResponse.data || []).filter((item) => item.active).length,
                    assistants: (assistantResponse.data || []).filter((item) => item.active).length,
                    teams: (teamResponse.data || []).filter((item) => item.active).length
                })
            })
            .catch(() => setError('Unable to load the scheduling setup. Please try again.'))
    }, [])

    async function handleGenerate() {
        setError('')

        if (offices.length === 0) {
            setError('Add at least one office before generating a schedule.')
            return
        }
        if (counts.doctors === 0 || counts.assistants === 0) {
            setError('Add at least one active doctor and assistant before generating a schedule.')
            return
        }

        setLoading(true)
        try {
            const existingResponse = await getAllCalendars()
            const existing = (existingResponse.data || []).filter(
                (calendar) => calendar.month === monthLabel
            )

            if (existing.length > 0) {
                navigate(`/manager/schedule?month=${monthValue}&existing=1`)
                return
            }

            const createdById = getLoggedInUserId()
            const startDate = formatDate(monthDate)
            const endDate = formatDate(new Date(
                monthDate.getFullYear(),
                monthDate.getMonth() + 1,
                0
            ))

            await Promise.all(offices.map((office) =>
                generateCalendar({
                    month: monthLabel,
                    startCalendarDate: startDate,
                    endCalendarDate: endDate,
                    createdById,
                    officeId: office.id
                })
            ))

            navigate(`/manager/schedule?month=${monthValue}&generated=1`)
        } catch (requestError) {
            console.error('Failed to generate monthly draft', requestError)
            setError(
                requestError.response?.data?.message
                || 'The monthly draft could not be generated. Check your doctors, assistants, teams, and office assignments.'
            )
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="calendar-page">
            <ManagerHeader />
            <main className="schedule-setup-shell">
                <header className="schedule-setup-heading">
                    <p className="eyebrow">Create monthly schedule</p>
                    <h1>Generate a starting draft</h1>
                    <p>Choose a month to create an editable draft for every location.</p>
                </header>

                <ol className="schedule-flow-steps" aria-label="Schedule creation steps">
                    <li className="active"><span>1</span><strong>Choose month</strong></li>
                    <li><span>2</span><strong>Review each day</strong></li>
                    <li><span>3</span><strong>Publish</strong></li>
                </ol>

                <section className="schedule-setup-card">
                    <label className="schedule-month-field">
                        Month to schedule
                        <input
                            type="month"
                            value={monthValue}
                            min={`${today.getFullYear() - 1}-01`}
                            max={`${today.getFullYear() + 2}-12`}
                            onChange={(event) => setMonthValue(event.target.value)}
                        />
                    </label>

                    <div className="schedule-generation-summary">
                        <p><strong>{monthLabel}</strong> will use your current scheduling setup:</p>
                        <div>
                            <span><strong>{counts.doctors}</strong> active doctors</span>
                            <span><strong>{counts.assistants}</strong> active assistants</span>
                            <span><strong>{counts.teams}</strong> reusable teams</span>
                            <span><strong>{offices.length}</strong> locations</span>
                        </div>
                    </div>

                    <p className="schedule-random-note">You can adjust any day before publishing.</p>

                    {error && <p className="error-message">{error}</p>}

                    <div className="schedule-setup-actions">
                        <button
                            type="button"
                            className="save-draft-btn"
                            onClick={() => navigate('/manager/dashboard')}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="publish-btn"
                            onClick={handleGenerate}
                            disabled={loading || !monthValue}
                        >
                            {loading ? 'Generating draft…' : 'Generate editable draft'}
                        </button>
                    </div>
                </section>
            </main>
        </div>
    )
}

export default ManagerNewCalendarPage
