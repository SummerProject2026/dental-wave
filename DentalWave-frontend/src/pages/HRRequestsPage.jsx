import '../App.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { getAllTimeOffRequests } from '../services/TimeOffRequestService'

function HRRequestsPage() {
    const navigate = useNavigate()
    const [requests, setRequests] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [filterBy, setFilterBy] = useState('all')
    const [error, setError] = useState('')

    useEffect(() => {
        getAllTimeOffRequests()
            .then((response) => {
                setRequests(response.data || [])
            })
            .catch((error) => {
                console.error('Unable to load time off requests:', error)
                setError('Unable to load time off requests.')
            })
    }, [])

    function formatSubmitted(dateTimeStr) {
        if (!dateTimeStr) return ''
        const date = new Date(dateTimeStr)
        if (isNaN(date.getTime())) return dateTimeStr
        return date.toLocaleDateString()
    }

    const filteredRequests = requests.filter((req) => {
        const name = `${req.employeeFirstName || req.firstName || ''} ${req.employeeLastName || req.lastName || ''}`.toLowerCase()

        const matchesSearch = name.includes(searchTerm.toLowerCase())

        const matchesFilter =
            filterBy === 'all' ||
            (filterBy === 'pending' && req.status?.toLowerCase() === 'pending') ||
            (filterBy === 'approved' && req.status?.toLowerCase() === 'approved') ||
            (filterBy === 'denied' && req.status?.toLowerCase() === 'denied') ||
            (filterBy === 'emergency' && req.emergency)

        return matchesSearch && matchesFilter
    })

    return (
        <div className="hr-page">
            <HRHeader />

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
                        <option value="all">Status</option>
                        <option value="pending">Pending</option>
                        <option value="approved">Approved</option>
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
                            filteredRequests.map((req) => {
                                const firstName = req.employeeFirstName || req.firstName || ''
                                const lastName = req.employeeLastName || req.lastName || ''
                                const employeeName = `${firstName} ${lastName}`.trim()

                                return (
                                    <tr key={req.id} className="request-row">
                                        <td>{employeeName || req.employeeName || 'Unknown Employee'}</td>
                                        <td>{req.startDate} – {req.endDate}</td>
                                        <td>{req.status}</td>
                                        <td>{req.emergency ? 'YES' : 'NO'}</td>
                                        <td>{formatSubmitted(req.submittedAt || req.submittedDate)}</td>
                                        <td>
                                            <button
                                                className="view-request-btn"
                                                onClick={() => navigate(`/hr/requests/${req.id}`)}
                                            >
                                                Review
                                            </button>
                                        </td>
                                    </tr>
                                )
                            })
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

export default HRRequestsPage