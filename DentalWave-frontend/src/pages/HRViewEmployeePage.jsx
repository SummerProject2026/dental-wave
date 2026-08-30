import '../App.css'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router'
import HRHeader from '../components/HRHeader'
import ManagerHeader from '../components/ManagerHeader'
import { getEmployeeById } from '../services/EmployeeService'
import { getTimeOffRequestsByEmployee } from '../services/TimeOffRequestService'
import { formatPhoneNumber } from '../utils/phoneUtils'

function HRViewEmployeePage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const location = useLocation()
    const isManagerView = location.pathname.startsWith('/manager')

    const [employee, setEmployee] = useState(null)
    const [requests, setRequests] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        getEmployeeById(id)
            .then((response) => {
                setEmployee(response.data)
            })
            .catch((error) => {
                console.error('Error loading employee:', error)
                setError('We could not load this employee. Please return to the employee list and try again.')
            })
            .finally(() => {
                setLoading(false)
            })

        getTimeOffRequestsByEmployee(id)
            .then((response) => setRequests(response.data || []))
            .catch((error) => {
                console.error('Error loading employee requests:', error)
                setRequests([])
            })
    }, [id])

    function formatDate(dateStr) {
        if (!dateStr) return ''
        return String(dateStr).split('T')[0]
    }

    const Header = isManagerView ? ManagerHeader : HRHeader
    const backPath = isManagerView ? '/manager/employees' : '/hr/employees'

    if (loading) {
        return (
            <div className="hr-page">
                <Header />
                <main className="view-employee-content">
                    <p>Loading employee...</p>
                </main>
            </div>
        )
    }

    if (error || !employee) {
        return (
            <div className="hr-page">
                <Header />
                <main className="view-employee-content">
                    <p className="error-message">{error || 'Employee not found.'}</p>
                    <button
                        type="button"
                        className="close-view-btn"
                        onClick={() => navigate(backPath)}
                    >
                        Close
                    </button>
                </main>
            </div>
        )
    }

    return (
        <div className="hr-page">
            <Header />

            <main className="view-employee-page">
                <section className="view-employee-card">
                    <div className="view-employee-left">
                        <h2>Employee Information</h2>

                        <div className="view-line"></div>

                        <div className="view-field">
                            <strong>First Name</strong>
                            <span>{employee.firstName}</span>
                        </div>

                        <div className="view-field">
                            <strong>Last Name</strong>
                            <span>{employee.lastName}</span>
                        </div>

                        <div className="view-field">
                            <strong>Username</strong>
                            <span>{employee.username}</span>
                        </div>

                        <h2>Contact</h2>
                        <div className="view-line"></div>

                        <div className="view-field">
                            <strong>Phone Number</strong>
                            <span>{employee.phoneNumber ? formatPhoneNumber(employee.phoneNumber) : 'N/A'}</span>
                        </div>

                        <div className="view-field">
                            <strong>Email</strong>
                            <span>{employee.email}</span>
                        </div>

                        <h2>Employment Details</h2>
                        <div className="view-line"></div>

                        <div className="view-details-grid">
                            <div className="view-field">
                                <strong>Role</strong>
                                <span>{employee.position}</span>
                            </div>

                            <div className="view-field">
                                <strong>Hire Date</strong>
                                <span>{employee.hireDate}</span>
                            </div>

                            <div className="view-field">
                                <strong>Office</strong>
                                {employee.offices?.length > 0 ? (
                                    employee.offices.map((office) => (
                                        <span key={office.id}>✓ {office.name}</span>
                                    ))
                                ) : (
                                    <span>No offices assigned</span>
                                )}
                            </div>

                            <div className="view-field">
                                <strong>Responsibilities</strong>
                                {employee.responsibilities?.length > 0 ? (
                                    <ul>
                                        {employee.responsibilities.map((item, index) => (
                                            <li key={index}>{item}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <span>No responsibilities listed</span>
                                )}
                            </div>

                            <div className="view-field">
                                <strong>Time-Off Balance</strong>
                                <span>{employee.timeOff} days</span>
                            </div>

                            <div className="view-field">
                                <strong>Account Status</strong>
                                <span>{employee.status}</span>
                            </div>
                        </div>
                    </div>

                    <div className="view-employee-right">
                        <h2>Time Off Requests:</h2>

                        <table className="view-timeoff-table">
                            <thead>
                            <tr>
                                <th>Dates</th>
                                <th>Status</th>
                                <th>Emergency</th>
                                <th>Submitted</th>
                                <th>Review Comments</th>
                            </tr>
                            </thead>

                            <tbody>
                            {requests.length > 0 ? (
                                requests.map((request) => (
                                    <tr key={request.id}>
                                        <td>{request.startDate} - {request.endDate}</td>
                                        <td>{request.status}</td>
                                        <td>{request.emergency ? 'Yes' : 'No'}</td>
                                        <td>{formatDate(request.submittedAt)}</td>
                                        <td>{request.reviewComment || ''}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td
                                        colSpan="5"
                                        style={{
                                            textAlign: 'center',
                                            fontStyle: 'italic',
                                            padding: '40px'
                                        }}
                                    >
                                        No requests made
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </section>

                <div className="view-employee-actions">
                    <button
                        type="button"
                        className="close-view-btn"
                        onClick={() => navigate(backPath)}
                    >
                        Close
                    </button>
                </div>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HRViewEmployeePage
