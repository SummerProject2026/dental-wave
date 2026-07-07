import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { getEmployeeById, resetEmployeePassword, updateEmployee } from '../services/EmployeeService'
import { getAllOffices } from '../services/OfficeService'
import { digitsOnly, formatPhoneNumber } from '../utils/phoneUtils'

function HREditEmployeePage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const [temporaryPassword, setTemporaryPassword] = useState('')
    const [resetError, setResetError] = useState('')
    const [offices, setOffices] = useState([])
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState('')
    const [saveError, setSaveError] = useState('')

    const [employee, setEmployee] = useState({
        id: '',
        userId: '',
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
        setLoading(true)
        setError('')

        Promise.all([getEmployeeById(id), getAllOffices()])
            .then((response) => {
                const employeeResponse = response[0]
                const officeResponse = response[1]
                const data = employeeResponse.data

                setOffices(officeResponse.data || [])

                setEmployee({
                    id: data.id || '',
                    userId: data.userId || '',
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
                    timeOff: data.timeOff ?? '',
                    officeIds: data.offices
                        ? data.offices.map(office => String(office.id))
                        : [],
                    status: data.status || 'ACTIVE'
                })
            })
            .catch((error) => {
                console.error('Error loading employee:', error)
                if (error.response?.status === 404) {
                    setError('Employee could not be found. Please return to the employee list and try again.')
                } else {
                    setError('Unable to load employee information. Please return to the employee list and try again.')
                }
            })
            .finally(() => setLoading(false))
    }, [id])

    function handleChange(event) {
        const { name, value } = event.target

        setEmployee({
            ...employee,
            [name]: name === 'phoneNumber' ? digitsOnly(value).slice(0, 10) : value
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

    function buildUpdatedEmployee(statusOverride = null) {
        return {
            id: employee.id,
            userId: employee.userId,
            firstName: employee.firstName,
            lastName: employee.lastName,
            username: employee.username,
            phoneNumber: employee.phoneNumber,
            email: employee.email,
            position: employee.position,
            hireDate: employee.hireDate,
            timeOff: employee.timeOff === '' ? 0.0 : Number(employee.timeOff),
            status: statusOverride || employee.status,
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
    }

    function handleSubmit(event) {
        event.preventDefault()
        setSaveError('')

        const employeeId = employee.id || id
        if (!employeeId) {
            setSaveError('Employee could not be found. Please return to the employee list and try again.')
            return
        }

        const updatedEmployee = buildUpdatedEmployee()

        setSaving(true)
        updateEmployee(employeeId, updatedEmployee)
            .then(() => {
                navigate(`/hr/employees/${employeeId}`)
            })
            .catch((error) => {
                console.error('Error updating employee:', error)
                if (error.response?.status === 404) {
                    setSaveError('Employee could not be found. Please return to the employee list and try again.')
                } else if (error.response?.data?.message) {
                    setSaveError(error.response.data.message)
                } else {
                    setSaveError('Unable to update employee. Please verify the information and try again.')
                }
            })
            .finally(() => setSaving(false))
    }

    function handleStatusToggle() {
        const newStatus = employee.status === 'ACTIVE'
            ? 'INACTIVE'
            : 'ACTIVE'

        const updatedEmployee = buildUpdatedEmployee(newStatus)

        const employeeId = employee.id || id

        setSaving(true)
        updateEmployee(employeeId, updatedEmployee)
            .then(() => {
                navigate(`/hr/employees/${employeeId}`)
            })
            .catch((error) => {
                console.error('Error updating employee status:', error)
                if (error.response?.status === 404) {
                    setSaveError('Employee could not be found. Please return to the employee list and try again.')
                } else {
                    setSaveError('Unable to update employee status. Please try again.')
                }
            })
            .finally(() => setSaving(false))
    }

    function handleResetPassword() {
        setTemporaryPassword('')
        setResetError('')

        if (!window.confirm('Reset this employee password and generate a temporary password?')) {
            return
        }

        resetEmployeePassword(id)
            .then((response) => {
                setTemporaryPassword(response.data)
            })
            .catch((error) => {
                console.error('Error resetting employee password:', error)
                setResetError('Password reset failed. Please try again.')
            })
    }

    if (loading) {
        return (
            <div className="hr-page">
                <HRHeader />
                <main className="add-employee-content">
                    <p>Loading employee...</p>
                </main>
            </div>
        )
    }

    if (error) {
        return (
            <div className="hr-page">
                <HRHeader />
                <main className="add-employee-content">
                    <p className="error-message">{error}</p>
                    <button
                        type="button"
                        className="cancel-employee-btn"
                        onClick={() => navigate('/hr/employees')}
                    >
                        Back to Employees
                    </button>
                </main>
            </div>
        )
    }

    return (
        <div className="hr-page">
            <HRHeader />

            <main className="add-employee-content">
                <form className="add-employee-card" onSubmit={handleSubmit}>
                    {saveError && <p className="error-message">{saveError}</p>}

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
                            <input name="phoneNumber" value={formatPhoneNumber(employee.phoneNumber)} onChange={handleChange} />
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
                                    {offices.length > 0 ? (
                                        offices.map((office) => {
                                            const officeId = String(office.id)

                                            return (
                                                <tr key={office.id}>
                                                    <td>
                                                        <input
                                                            type="checkbox"
                                                            checked={employee.officeIds.includes(officeId)}
                                                            onChange={() => toggleOffice(officeId)}
                                                        />
                                                    </td>
                                                    <td>{office.name}</td>
                                                </tr>
                                            )
                                        })
                                    ) : (
                                        <tr>
                                            <td colSpan="2">No offices found.</td>
                                        </tr>
                                    )}
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

                        <button type="submit" className="save-employee-btn" disabled={saving}>
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
                        disabled={saving}
                    >
                        {employee.status === 'ACTIVE'
                            ? 'Deactivate Employee'
                            : 'Activate Employee'}
                    </button>
                </form>

                <div className="view-employee-actions">
                    <button
                        type="button"
                        className="reset-password-btn"
                        onClick={handleResetPassword}
                    >
                        Reset Password
                    </button>
                </div>

                {temporaryPassword && (
                    <div className="temporary-password-box">
                        Temporary password: {temporaryPassword}
                    </div>
                )}

                {resetError && <p className="error-message">{resetError}</p>}
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HREditEmployeePage
