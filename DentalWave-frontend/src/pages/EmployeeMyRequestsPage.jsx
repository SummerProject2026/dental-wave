import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import EmployeeHeader from '../components/EmployeeHeader'
import { getTimeOffRequestsByEmployee } from '../services/TimeOffRequestService'

function EmployeeMyRequestsPage() {
    const navigate = useNavigate()

    const [requests, setRequests] = useState([])
    const [error, setError] = useState('')

    // Temporary until login/auth is connected
    const employeeId = 1

    useEffect(() => {
        loadRequests()
    }, [])

    function loadRequests() {
        getTimeOffRequestsByEmployee(employeeId)
            .then((response) => {
                setRequests(response.data)
            })
            .catch((error) => {
                console.error(error)
                setError('Unable to load time off requests.')
            })
    }

    function viewRequest(requestId) {
        navigate(`/employee/requests/${requestId}`)
    }

    return (
        <div className="employee-calendar-page">

            <EmployeeHeader />

            <main className="calendar-content">

                <h1 className="page-title">My Requests</h1>

                {error && <p className="error-message">{error}</p>}

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
                        {requests.map((request) => (
                            <tr
                                key={request.id}
                                className="request-row"
                                onClick={() => viewRequest(request.id)}
                            >
                                <td>
                                    {request.startDate} - {request.endDate}
                                </td>
                                <td>{request.submittedDate}</td>
                                <td>{request.status}</td>
                                <td>{request.emergency ? 'YES' : 'NO'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

            </main>

            <footer className="page-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default EmployeeMyRequestsPage