import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import EmployeeHeader from '../components/EmployeeHeader'
import { getTimeOffRequestsByEmployee } from '../services/TimeOffRequestService'

function EmployeeMyRequestsPage() {
    const navigate = useNavigate()
    const [requests, setRequests] = useState([])
    const [error, setError] = useState('')


    // TODO: replace with real logged-in employee id (from auth context/storage)
    const employeeId = Number(sessionStorage.getItem('employeeId'))

    function loadRequests() {
        getTimeOffRequestsByEmployee(employeeId)
            .then((response) => setRequests(response.data))
            .catch((error) => {
                console.error(error)
                setError('Unable to load time off requests.')
            })
    }

    useEffect(() => { loadRequests() }, [])

    // Format LocalDateTime ("2026-06-15T10:23:00") -> "6/15/2026"
    function formatSubmitted(dateTimeStr) {
        if (!dateTimeStr) return ''
        const date = new Date(dateTimeStr)
        if (isNaN(date.getTime())) return dateTimeStr
        return date.toLocaleDateString()
    }

    return (
        <div className="employee-requests-page">
            <EmployeeHeader />

            <main className="requests-content">
                <h1 className="page-title">My Requests</h1>
                <hr className="page-title-underline" />

                {error && <p className="error-message">{error}</p>}

                <button
                    className="new-request-btn"
                    onClick={() => navigate('/employee/requests/new')}
                >
                    + New Request
                </button>

                <div className="requests-table-container">
                    <table className="requests-table">
                        <thead>
                        <tr>
                            <th>Dates</th>
                            <th>Submitted</th>
                            <th>Status</th>
                            <th>Emergency</th>
                        </tr>
                        </thead>
                        <tbody>
                        {requests.length > 0 ? requests.map((request) => (
                            <tr
                                key={request.id}
                                className="request-row"
                                onClick={() => navigate(`/employee/requests/${request.id}`)}
                            >
                                <td>{request.startDate} – {request.endDate}</td>
                                <td>{formatSubmitted(request.submittedAt)}</td>
                                <td>{request.status}</td>
                                <td>{request.emergency ? 'YES' : 'NO'}</td>
                            </tr>
                        )) : (
                            <tr>
                                <td colSpan="4" className="empty-requests-message">
                                    No requests found.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default EmployeeMyRequestsPage
