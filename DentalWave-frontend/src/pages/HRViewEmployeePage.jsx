import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { getEmployeeById } from '../services/EmployeeService'

function HRViewEmployeePage() {
    const { id } = useParams()
    const navigate = useNavigate()

    const [employee, setEmployee] = useState(null)

    useEffect(() => {
        getEmployeeById(id)
            .then((response) => {
                setEmployee(response.data)
            })
            .catch((error) => {
                console.error('Error loading employee:', error)
            })
    }, [id])

    if (!employee) {
        return (
            <div className="hr-page">
                <HRHeader />
                <main className="view-employee-content">
                    <p>Loading employee...</p>
                </main>
            </div>
        )
    }

    return (
        <div className="hr-page">
            <HRHeader />

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
                            <span>{employee.phoneNumber || 'N/A'}</span>
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
                            </tr>
                            </thead>

                            <tbody>
                            {employee.timeOffRequests?.length > 0 ? (
                                employee.timeOffRequests.map((request) => (
                                    <tr key={request.id}>
                                        <td>{request.startDate} - {request.endDate}</td>
                                        <td>{request.status}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td
                                        colSpan="2"
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

                <button
                    type="button"
                    className="close-view-btn"
                    onClick={() => navigate('/hr/employees')}
                >
                    Close
                </button>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HRViewEmployeePage