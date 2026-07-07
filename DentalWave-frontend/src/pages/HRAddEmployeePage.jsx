import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { createEmployee } from '../services/EmployeeService'
import { getLoggedInUserRole, getToken } from '../services/AuthService'
import { getAllOffices } from '../services/OfficeService'
import { digitsOnly, formatPhoneNumber } from '../utils/phoneUtils'

function HRAddEmployeePage() {
    const navigate = useNavigate()

    const [errors, setErrors] = useState({})
    const [submitError, setSubmitError] = useState('')
    const [offices, setOffices] = useState([])
    const [officesLoading, setOfficesLoading] = useState(true)

    // Stores all form values for the new user account and employee profile
    const [employee, setEmployee] = useState({
        firstName: '',
        lastName: '',
        username: '',
        phoneNumber: '',
        email: '',
        temporaryPassword: '',
        role: 'ASSISTANT',
        position: 'Assistant',
        hireDate: '',
        responsibilities: '',
        timeOffBalance: '',
        officeIds: [],
        status: 'ACTIVE'
    })

    useEffect(() => {
        setOfficesLoading(true)

        getAllOffices()
            .then((response) => {
                setOffices(response.data || [])
            })
            .catch((error) => {
                console.error('Error loading offices:', error)
                setSubmitError('Unable to load office locations. Please refresh and try again.')
            })
            .finally(() => setOfficesLoading(false))
    }, [])

    // Updates normal input/select values
    function handleChange(event) {
        const { name, value } = event.target
        setErrors({ ...errors, [name]: '' })
        setEmployee({
            ...employee,
            [name]: name === 'phoneNumber' ? digitsOnly(value).slice(0, 10) : value
        })
    }

    function validate() {
        const newErrors = {}

        if (!employee.email.endsWith('@gmail.com')) {
            newErrors.email = 'Email must end with @gmail.com'
        }

        if (employee.phoneNumber && !/^\d{10}$/.test(employee.phoneNumber)) {
            newErrors.phoneNumber = 'Phone number must be exactly 10 digits'
        }

        if (employee.officeIds.length === 0) {
            newErrors.officeIds = 'Please select at least one office.'
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    // Adds or removes an office from the selected office list
    function toggleOffice(officeId) {
        const officeIdString = String(officeId)
        const officeSelected = employee.officeIds.includes(officeIdString)
        setErrors({ ...errors, officeIds: '' })

        setEmployee({
            ...employee,
            officeIds: officeSelected
                ? employee.officeIds.filter(id => id !== officeIdString)
                : [...employee.officeIds, officeIdString]
        })
    }

    function getEmployeeCreateErrorMessage(error) {
        const status = error.response?.status
        const responseMessage = typeof error.response?.data === 'string'
            ? error.response.data
            : error.response?.data?.message
        const normalizedMessage = String(responseMessage || '').toLowerCase()

        if (status === 401 || status === 403) {
            return 'Your session is not authorized to create employees.\nPlease log out and sign back in as an HR or Admin user.'
        }

        if (status === 400) {
            if (normalizedMessage.includes('username')) {
                return 'Unable to create employee.\nThe username is already being used by another employee.'
            }

            if (normalizedMessage.includes('email')) {
                return 'Unable to create employee.\nThe email address is already being used by another employee.'
            }

            return responseMessage
                ? `Unable to create employee.\n${responseMessage}`
                : 'Unable to create employee.\nPlease verify the information entered and try again.'
        }

        if (status >= 500) {
            return 'Unable to create employee.\nThe request could not be completed due to a system error.'
        }

        return 'Unable to create employee.\n\nPossible causes:\n• Username already exists\n• Email already exists\n• Session expired\n• Temporary system issue\n\nPlease review the information and try again.'
    }

    // Builds the CreateEmployeeDto expected by the backend
    function handleSubmit(event) {
        event.preventDefault()
        setSubmitError('')

        const token = getToken()
        const role = getLoggedInUserRole()
        if (!token || !['ROLE_HR', 'ROLE_ADMIN'].includes(role)) {
            setSubmitError('Your login session is not authorized to create employees. Please log out and log back in as HR or Admin.')
            return
        }

        if (!validate()) return
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
                position: employee.position,
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
                console.error('Employee creation failed:', error)
                setSubmitError(getEmployeeCreateErrorMessage(error))
            })
    }

    return (
        <div className="hr-page">
            <HRHeader />

            <main className="add-employee-content">
                <form className="add-employee-card" onSubmit={handleSubmit} autoComplete="off">
                    {submitError && <p className="error-message">{submitError}</p>}

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
                            <input name="phoneNumber" value={formatPhoneNumber(employee.phoneNumber)} onChange={handleChange} autoComplete="off" />
                            {errors.phoneNumber && <span className="field-error">{errors.phoneNumber}</span>}
                        </div>

                        <div className="form-row">
                            <label>Email</label>
                            <input type="email" name="email" value={employee.email} onChange={handleChange} required autoComplete="off" />
                            {errors.email && <span className="field-error">{errors.email}</span>}
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
                                <label>Position</label>
                                <select name="position" value={employee.position} onChange={handleChange}>
                                    <option value="Doctor">Doctor</option>
                                    <option value="TC">TC</option>
                                    <option value="Assistant">Assistant</option>
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
                                    {officesLoading ? (
                                        <tr>
                                            <td colSpan="2">Loading offices...</td>
                                        </tr>
                                    ) : offices.length > 0 ? (
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
                                {errors.officeIds && <span className="field-error">{errors.officeIds}</span>}
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
                                <label>Time-Off Balance (hours)</label>
                                <input type="number" name="timeOffBalance" value={employee.timeOffBalance} onChange={handleChange} min="0" />
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
