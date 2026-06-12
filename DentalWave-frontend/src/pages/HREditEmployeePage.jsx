import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { getEmployeeById, updateEmployee } from '../services/EmployeeService'

function HREditEmployeePage() {
    const { id } = useParams()
    const navigate = useNavigate()

    const [employee, setEmployee] = useState({
        firstName: '',
        lastName: '',
        username: '',
        phoneNumber: '',
        email: '',
        position: 'ASSISTANT',
        hireDate: '',
        responsibilities: '',
        timeOff: '',
        officeIds: [],
        status: 'ACTIVE'
    })

    useEffect(() => {
        getEmployeeById(id)
            .then((response) => {
                const data = response.data

                setEmployee({
                    firstName: data.firstName || '',
                    lastName: data.lastName || '',
                    username: data.username || '',
                    phoneNumber: data.phoneNumber || '',
                    email: data.email || '',
                    position: data.position || 'ASSISTANT',
                    hireDate: data.hireDate || '',
                    responsibilities: data.responsibilities
                        ? data.responsibilities.join(', ')
                        : '',
                    timeOff: data.timeOff || '',
                    officeIds: data.offices
                        ? data.offices.map(office => String(office.id))
                        : [],
                    status: data.status || 'ACTIVE'
                })
            })
            .catch((error) => {
                console.error('Error loading employee:', error)
            })
    }, [id])

    function handleChange(event) {
        const { name, value } = event.target

        setEmployee({
            ...employee,
            [name]: value
        })
    }

    function toggleOffice(officeId) {
        const selected = employee.officeIds.includes(officeId)

        setEmployee({
            ...employee,
            officeIds: selected
                ? employee.officeIds.filter(id => id !== officeId)
                : [...employee.officeIds, officeId]
        })
    }

    function handleSubmit(event) {
        event.preventDefault()

        const updatedEmployee = {
            firstName: employee.firstName,
            lastName: employee.lastName,
            username: employee.username,
            email: employee.email,
            phoneNumber: employee.phoneNumber,
            position: employee.position,
            hireDate: employee.hireDate,
            timeOff: employee.timeOff === '' ? 0.0 : Number(employee.timeOff),
            status: employee.status,
            responsibilities: employee.responsibilities === ''
                ? []
                : employee.responsibilities
                    .split(',')
                    .map(item => item.trim())
                    .filter(item => item !== ''),
            offices: employee.officeIds.map(officeId => ({
                id: Number(officeId)
            })),
            availabilities: []
        }

        updateEmployee(id, updatedEmployee)
            .then(() => {
                navigate('/hr/employees')
            })
            .catch((error) => {
                console.error('Error updating employee:', error)
            })
    }

    function handleStatusToggle() {
        const newStatus = employee.status === 'ACTIVE'
            ? 'INACTIVE'
            : 'ACTIVE'

        const updatedEmployee = {
            ...employee,
            status: newStatus,
            timeOff: employee.timeOff === '' ? 0.0 : Number(employee.timeOff),
            responsibilities: employee.responsibilities === ''
                ? []
                : employee.responsibilities
                    .split(',')
                    .map(item => item.trim())
                    .filter(item => item !== ''),
            offices: employee.officeIds.map(officeId => ({
                id: Number(officeId)
            })),
            availabilities: []
        }

        updateEmployee(id, updatedEmployee)
            .then(() => {
                navigate('/hr/employees')
            })
            .catch((error) => {
                console.error('Error updating employee status:', error)
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

                        <div className="form-row">
                            <label>Username</label>
                            <input name="username" value={employee.username} onChange={handleChange} required />
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
                    </section>

                    <section className="form-section">
                        <h2>Employment Details</h2>

                        <div className="details-grid">
                            <div className="form-row">
                                <label>Role</label>
                                <select name="position" value={employee.position} onChange={handleChange}>
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
                                                checked={employee.officeIds.includes('855')}
                                                onChange={() => toggleOffice('855')}
                                            />
                                        </td>
                                        <td>Raleigh</td>
                                    </tr>

                                    <tr>
                                        <td>
                                            <input
                                                type="checkbox"
                                                checked={employee.officeIds.includes('856')}
                                                onChange={() => toggleOffice('856')}
                                            />
                                        </td>
                                        <td>Garner</td>
                                    </tr>

                                    <tr>
                                        <td>
                                            <input
                                                type="checkbox"
                                                checked={employee.officeIds.includes('857')}
                                                onChange={() => toggleOffice('857')}
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
                                />
                            </div>

                            <div className="form-row">
                                <label>Time-Off Balance</label>
                                <input type="number" name="timeOff" value={employee.timeOff} onChange={handleChange} />
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

                    <button
                        type="button"
                        className={
                            employee.status === 'ACTIVE'
                                ? 'deactivate-employee-btn'
                                : 'activate-employee-btn'
                        }
                        onClick={handleStatusToggle}
                    >
                        {employee.status === 'ACTIVE'
                            ? 'Deactivate Employee'
                            : 'Activate Employee'}
                    </button>
                </form>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HREditEmployeePage