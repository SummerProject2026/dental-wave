import '../App.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { createEmployee } from '../services/EmployeeService'

function HRAddEmployeePage() {
    const navigate = useNavigate()

    // Stores all form values for the new user account and employee profile
    const [employee, setEmployee] = useState({
        firstName: '',
        lastName: '',
        username: '',
        phoneNumber: '',
        email: '',
        temporaryPassword: '',
        role: 'ASSISTANT',
        hireDate: '',
        responsibilities: '',
        timeOffBalance: '',
        officeIds: ['1'],
        status: 'ACTIVE'
    })

    // Updates normal input/select values
    function handleChange(event) {
        const { name, value } = event.target

        setEmployee({
            ...employee,
            [name]: value
        })
    }

    // Updates selected offices from the multi-select
    function handleOfficeChange(event) {
        const selectedOfficeIds = Array.from(
            event.target.selectedOptions,
            option => option.value
        )

        setEmployee({
            ...employee,
            officeIds: selectedOfficeIds
        })
    }

    // Adds or removes an office from the selected office list
    function toggleOffice(officeId) {
        const officeSelected = employee.officeIds.includes(officeId)

        setEmployee({
            ...employee,
            officeIds: officeSelected
                ? employee.officeIds.filter(id => id !== officeId)
                : [...employee.officeIds, officeId]
        })
    }

    // Builds the CreateEmployeeDto expected by the backend
    function handleSubmit(event) {
        event.preventDefault()
        const employeeToCreate = {
            user: {
                firstName: employee.firstName,
                lastName: employee.lastName,
                username: employee.username,
                email: employee.email,
                phoneNumber: employee.phoneNumber,
                password: employee.temporaryPassword
            },

            role: `ROLE_${employee.role}`,

            employee: {
                firstName: employee.firstName,
                lastName: employee.lastName,
                email: employee.email,
                position: employee.role,
                hireDate: employee.hireDate,
                timeOff: employee.timeOffBalance === ''
                    ? 0.0
                    : Number(employee.timeOffBalance),
                status: employee.status,

                responsibilities: employee.responsibilities === ''
                    ? []
                    : employee.responsibilities
                        .split(',')
                        .map(item => item.trim())
                        .filter(item => item !== ''),

                offices: employee.officeIds.map(id => ({
                    id: Number(id)
                })),

                availabilities: []
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
                        <h2>Account and Contact</h2>

                        <div className="form-row">
                            <label>Username</label>
                            <input name="username" value={employee.username} onChange={handleChange} required />
                        </div>

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
                                <input type="date" name="hireDate" value={employee.hireDate} onChange={handleChange} required />
                            </div>

                            <div className="form-row office-row">
                                <label>Office</label>

                                <table className="office-table">
                                    <tbody>
                                    <tr>
                                        <td>
                                            <input
                                                type="checkbox"
                                                checked={employee.officeIds.includes('1')}
                                                onChange={() => toggleOffice('1')}
                                            />
                                        </td>
                                        <td>Raleigh</td>
                                    </tr>

                                    <tr>
                                        <td>
                                            <input
                                                type="checkbox"
                                                checked={employee.officeIds.includes('2')}
                                                onChange={() => toggleOffice('2')}
                                            />
                                        </td>
                                        <td>Garner</td>
                                    </tr>

                                    <tr>
                                        <td>
                                            <input
                                                type="checkbox"
                                                checked={employee.officeIds.includes('3')}
                                                onChange={() => toggleOffice('3')}
                                            />
                                        </td>
                                        <td>Smithfield</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>

                            <div className="form-row">
                                <label>Responsibilities</label>
                                <input
                                    name="responsibilities"
                                    value={employee.responsibilities}
                                    onChange={handleChange}
                                    placeholder="Scheduling, Sterilization, Front desk"
                                />
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