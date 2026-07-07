import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import logo from '../pictures/wake-logo.png'
import EmployeeHeader from '../components/EmployeeHeader'
import { getEmployeeById, updateEmployee } from '../services/EmployeeService'
import { digitsOnly, formatPhoneNumber } from '../utils/phoneUtils'

function EmployeeEditProfilePage() {
    const navigate = useNavigate()

    const employeeId = Number(sessionStorage.getItem('employeeId'))

    const [originalEmployee, setOriginalEmployee] = useState(null)

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        phoneNumber: '',
        password: '',
        repeatPassword: '',
        status: '',
        hireDate: '',
        timeOff: ''
    })

    const [error, setError] = useState('')

    useEffect(() => {
        getEmployeeById(employeeId)
            .then((response) => {
                const employee = response.data

                setOriginalEmployee(employee)

                setFormData({
                    firstName: employee.firstName || '',
                    lastName: employee.lastName || '',
                    username: employee.username || '',
                    email: employee.email || '',
                    phoneNumber: employee.phoneNumber || '',
                    password: '',
                    repeatPassword: '',
                    status: employee.status || '',
                    hireDate: employee.hireDate || '',
                    timeOff: employee.timeOff ?? ''
                })
            })
            .catch((error) => {
                console.error('Unable to load employee profile:', error)
                setError('Unable to load employee profile.')
            })
    }, [employeeId])

    function handleChange(event) {
        const { name, value } = event.target

        setFormData({
            ...formData,
            [name]: name === 'phoneNumber' ? digitsOnly(value).slice(0, 10) : value
        })
    }

    function handleSubmit(event) {
        event.preventDefault()
        setError('')

        if (!originalEmployee) {
            setError('Employee profile has not finished loading.')
            return
        }

        if (formData.password !== formData.repeatPassword) {
            setError('Passwords do not match.')
            return
        }

        const updatedEmployee = {
            ...originalEmployee,

            firstName: formData.firstName,
            lastName: formData.lastName,
            username: formData.username,
            email: formData.email,
            phoneNumber: formData.phoneNumber,
            password: formData.password === '' ? null : formData.password,

            status: originalEmployee.status,
            hireDate: originalEmployee.hireDate,
            timeOff: originalEmployee.timeOff,
            position: originalEmployee.position,
            responsibilities: originalEmployee.responsibilities || [],
            offices: originalEmployee.offices || [],
            availabilities: originalEmployee.availabilities || []
        }

        updateEmployee(employeeId, updatedEmployee)
            .then(() => {
                navigate('/employee/profile')
            })
            .catch((error) => {
                console.error('Unable to update employee profile:', error)
                setError('Unable to update employee profile.')
            })
    }

    const fullName = `${formData.firstName} ${formData.lastName}`.trim()

    return (
        <div className="profile-page">
            <EmployeeHeader />

            <main className="profile-layout">
                <aside className="profile-sidebar">
                    <img
                        src={logo}
                        alt="Wake Orthodontics"
                        className="profile-logo"
                    />
                </aside>

                <section className="profile-content">
                    <div className="profile-top">
                        <div className="tooth-icon">🦷</div>

                        <div>
                            <h3>{fullName || 'Your Name'}</h3>
                            <p>{formData.username || 'username'}</p>
                        </div>
                    </div>

                    {error && <p className="error-message">{error}</p>}

                    <form className="profile-form" onSubmit={handleSubmit}>
                        <div className="profile-column">
                            <div className="profile-row">
                                <span>First Name:</span>
                                <input
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Last Name:</span>
                                <input
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>User Name:</span>
                                <input
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Email:</span>
                                <input
                                    name="email"
                                    type="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Phone Number:</span>
                                <input
                                    name="phoneNumber"
                                    value={formatPhoneNumber(formData.phoneNumber)}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Password:</span>
                                <input
                                    name="password"
                                    type="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="Leave blank to keep current password"
                                />
                            </div>

                            <div className="profile-row">
                                <span>Repeat Password:</span>
                                <input
                                    name="repeatPassword"
                                    type="password"
                                    value={formData.repeatPassword}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Status:</span>
                                <input value={formData.status} readOnly />
                            </div>

                            <div className="profile-row">
                                <span>Hire Date:</span>
                                <input value={formData.hireDate} readOnly />
                            </div>

                            <div className="profile-row">
                                <span>PTO:</span>
                                <input value={formData.timeOff} readOnly />
                            </div>

                            <button type="submit" className="save-profile-button">
                                Save
                            </button>
                        </div>
                    </form>
                </section>
            </main>

            <footer className="calendar-footer">
                © All Rights Reserved
            </footer>
        </div>
    )
}

export default EmployeeEditProfilePage
