import '../App.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { createEmployee } from '../services/EmployeeService'

function HRAddEmployeePage() {
    const navigate = useNavigate()

    const [employee, setEmployee] = useState({
        firstName: '',
        lastName: '',
        phoneNumber: '',
        email: '',
        temporaryPassword: '',
        role: 'ASSISTANT',
        hireDate: '',
        responsibilities: '',
        timeOffBalance: '',
        office: 'RALEIGH',
        status: 'ACTIVE'
    })

    function handleChange(event) {
        const { name, value } = event.target

        setEmployee({
            ...employee,
            [name]: value
        })
    }

    function handleSubmit(event) {
        event.preventDefault()

        const employeeToCreate = {
            user: {
                firstName: employee.firstName,
                lastName: employee.lastName,
                username: employee.email,
                email: employee.email,
                phoneNumber: employee.phoneNumber,
                roles: [employee.role],
                enabled: true
            },
            employee: {
                position: employee.role,
                hireDate: employee.hireDate,
                timeOff: Number(employee.timeOffBalance),
                status: employee.status,
                responsibilities: employee.responsibilities
                    .split(',')
                    .map(item => item.trim())
                    .filter(item => item !== ''),
                offices: [
                    { id: Number(employee.officeId) }
                ]
            }
        }

        createEmployee(employeeToCreate)
            .then(() => {
                navigate('/hr/employees', { state: { employeeCreated: true } })
            })
            .catch((error) => {
                console.error('Error creating employee:', error)
            })
    }

    return (
        <div className="hr-page">
            <HRHeader />

            <main className="add-employee-content">
                <form className="add-employee-card" onSubmit={handleSubmit}>
                    <section className="form-section">
                        <h2>Employee Information</h2>

                        <div className="form-row">
                            <label>First Name</label>
                            <input name="firstName" value={employee.firstName} onChange={handleChange} required />
                        </div>

                        <div className="form-row">
                            <label>Last Name</label>
                            <input name="lastName" value={employee.lastName} onChange={handleChange} required />
                        </div>
                    </section>

                    <section className="form-section">
                        <h2>Contact</h2>

                        <div className="form-row">
                            <label>Phone Number</label>
                            <input name="phoneNumber" value={employee.phoneNumber} onChange={handleChange} />
                        </div>

                        <div className="form-row">
                            <label>Email</label>
                            <input type="email" name="email" value={employee.email} onChange={handleChange} required />
                        </div>

                        <div className="form-row">
                            <label>Temporary Password</label>
                            <input type="password" name="temporaryPassword" value={employee.temporaryPassword} onChange={handleChange} required />
                        </div>
                    </section>

                    <section className="form-section">
                        <h2>Employment Details</h2>

                        <div className="details-grid">
                            <div className="form-row">
                                <label>Role</label>
                                <select name="role" value={employee.role} onChange={handleChange}>
                                    <option value="ASSISTANT">Assistant</option>
                                    <option value="MANAGER">Manager</option>
                                    <option value="HR">HR</option>
                                    <option value="ADMIN">Admin</option>
                                </select>
                            </div>

                            <div className="form-row">
                                <label>Hire Date</label>
                                <input type="date" name="hireDate" value={employee.hireDate} onChange={handleChange} />
                            </div>

                            <div className="form-row">
                                <label>Office</label>
                                <select name="office" value={employee.office} onChange={handleChange}>
                                    <option value="RALEIGH">Raleigh</option>
                                    <option value="GARNER">Garner</option>
                                    <option value="SMITHFIELD">Smithfield</option>
                                </select>
                            </div>

                            <div className="form-row">
                                <label>Responsibilities</label>
                                <input name="responsibilities" value={employee.responsibilities} onChange={handleChange} />
                            </div>

                            <div className="form-row">
                                <label>Time-Off Balance</label>
                                <input type="number" name="timeOffBalance" value={employee.timeOffBalance} onChange={handleChange} />
                            </div>

                            <div className="form-row">
                                <label>Account Status</label>
                                <select name="status" value={employee.status} onChange={handleChange}>
                                    <option value="ACTIVE">Enabled</option>
                                    <option value="INACTIVE">Disabled</option>
                                </select>
                            </div>
                        </div>
                    </section>

                    <div className="add-employee-buttons">
                        <button type="button" className="cancel-employee-btn" onClick={() => navigate('/hr/employees')}>
                            Cancel
                        </button>

                        <button type="submit" className="save-employee-btn">
                            Save
                        </button>
                    </div>
                </form>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HRAddEmployeePage