import '../App.css'
import { useState, useEffect } from 'react'
import ManagerHeader from '../components/ManagerHeader'
import { getAllTimeOffRequests } from '../services/TimeOffRequestService'
import { removeEmployeeFromScheduleOnDates } from '../services/CalendarService'
import { getAllCalendars } from '../services/CalendarService'

function ManagerRequestsPage() {

    const [requests, setRequests] = useState([])
    const [calendars, setCalendars] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [filterBy, setFilterBy] = useState('approved')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [successId, setSuccessId] = useState(null)

    useEffect(() => {
        loadRequests()
        loadCalendars()
    }, [])

    function loadRequests() {
        getAllTimeOffRequests()
            .then((response) => setRequests(response.data || []))
            .catch((err) => console.error('Failed to load requests', err))
    }

    function loadCalendars() {
        getAllCalendars()
            .then((response) => setCalendars(response.data || []))
            .catch((err) => console.error('Failed to load calendars', err))
    }

    function getEmployeeName(req) {
        return `${req.employeeFirstName || ''} ${req.employeeLastName || ''}`.trim()
            || req.employeeName
            || 'Unknown'
    }

    const filteredRequests = requests.filter((req) => {
        const name = getEmployeeName(req).toLowerCase()
        const matchesSearch = name.includes(searchTerm.toLowerCase())
        const matchesFilter =
            filterBy === 'all' ||
            (filterBy === 'approved' && req.status?.toUpperCase() === 'APPROVED') ||
            (filterBy === 'pending' && req.status?.toUpperCase() === 'PENDING') ||
            (filterBy === 'denied' && req.status?.toUpperCase() === 'DENIED') ||
            (filterBy === 'emergency' && req.emergency)
        return matchesSearch && matchesFilter
    })

    /**
     * Finds the office id for the employee based on their calendar assignments.
     * Falls back to the first calendar's office if no specific match found.
     */
    function findOfficeIdForEmployee(employeeId) {
        for (const calendar of calendars) {
            if (!calendar.schedules) continue
            for (const schedule of calendar.schedules) {
                if (!schedule.teams) continue
                const teamValues = Object.values(schedule.teams)
                for (const teamEmployees of teamValues) {
                    if (teamEmployees.some((e) => e.id === employeeId)) {
                        return calendar.officeId
                    }
                }
            }
        }
        return null
    }

    async function handleRemoveFromSchedule(req) {
        setError('')
        setSuccessId(null)
        setLoading(true)

        try {
            const officeId = findOfficeIdForEmployee(req.employeeId)

            if (!officeId) {
                setError(`No scheduled shifts found for ${getEmployeeName(req)} during this period.`)
                setLoading(false)
                return
            }

            await removeEmployeeFromScheduleOnDates(
                officeId,
                req.employeeId,
                req.startDate,
                req.endDate
            )

            await loadCalendars()
            setSuccessId(req.id)
        } catch (err) {
            console.error('Failed to remove from schedule', err)
            setError('Failed to remove employee from schedule. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    function formatDate(dateStr) {
        if (!dateStr) return ''
        return new Date(dateStr).toLocaleDateString()
    }

    return (
        <div className="hr-page">

            <ManagerHeader />

            <main className="hr-requests-content">

                <div className="hr-requests-search-row">
                    <label>Search:</label>
                    <input
                        type="text"
                        placeholder="Name"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />

                    <label>Filter By:</label>
                    <select
                        value={filterBy}
                        onChange={(e) => setFilterBy(e.target.value)}
                    >
                        <option value="all">All</option>
                        <option value="approved">Approved</option>
                        <option value="pending">Pending</option>
                        <option value="denied">Denied</option>
                        <option value="emergency">Emergency</option>
                    </select>
                </div>

                {error && <p className="error-message">{error}</p>}

                <section className="hr-requests-table-section">
                    <table className="employee-table">
                        <thead>
                        <tr>
                            <th>Employee</th>
                            <th>Dates</th>
                            <th>Status</th>
                            <th>Emergency</th>
                            <th>Submitted</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredRequests.length > 0 ? (
                            filteredRequests.map((req) => (
                                <tr key={req.id} className="request-row">
                                    <td>{getEmployeeName(req)}</td>
                                    <td>{req.startDate} – {req.endDate}</td>
                                    <td>{req.status}</td>
                                    <td>{req.emergency ? 'YES' : 'NO'}</td>
                                    <td>{formatDate(req.submittedAt)}</td>
                                    <td>
                                        {req.status?.toUpperCase() === 'APPROVED' ? (
                                            successId === req.id ? (
                                                <span style={{ color: 'green', fontWeight: 600 }}>
                                                    ✓ Removed
                                                </span>
                                            ) : (
                                                <button
                                                    className="deactivate-employee-btn"
                                                    onClick={() => handleRemoveFromSchedule(req)}
                                                    disabled={loading}
                                                >
                                                    Remove from Schedule
                                                </button>
                                            )
                                        ) : (
                                            <span style={{ color: '#888', fontSize: 13 }}>
                                                —
                                            </span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6" className="empty-table-message">
                                    No requests found.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </section>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerRequestsPage