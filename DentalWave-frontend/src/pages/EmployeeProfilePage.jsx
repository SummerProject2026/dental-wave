import '../App.css'
import logo from '../pictures/wake-logo.png'
import EmployeeHeader from '../components/EmployeeHeader'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { getEmployeeById } from '../services/EmployeeService'
import { formatPhoneNumber } from '../utils/phoneUtils'
import { IconPencil } from '../components/Icons'

/**
 * EmployeeProfilePage
 *
 * Displays the currently logged-in employee's profile information.
 * Information is loaded from the backend and displayed in read-only fields.
 *
 * Employees may navigate to the edit page to update their profile.
 */
function EmployeeProfilePage() {

    const navigate = useNavigate()

    /** Stores employee profile data loaded from the backend */
    const [employee, setEmployee] = useState(null)

    /** Error message displayed if profile loading fails */
    const [error, setError] = useState('')

    /**
     * Temporary employee id.
     *
     * TODO:
     * Replace with employeeId stored during login:
     *
     * const employeeId =
     *     Number(sessionStorage.getItem('employeeId'))
     */
    const employeeId = Number(sessionStorage.getItem('employeeId'))

    /**
     * Retrieves employee information from the backend.
     */
    function loadEmployeeProfile() {
        getEmployeeById(employeeId)
            .then((response) => {
                setEmployee(response.data)
            })
            .catch((error) => {
                console.error('Unable to load employee profile:', error)
                setError('Unable to load employee profile.')
            })
    }

    /**
     * Load employee information when page first renders.
     */
    useEffect(() => {
        loadEmployeeProfile()
    }, [])

    /**
     * Builds employee full name from first and last name.
     */
    const fullName = employee
        ? `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
        : ''

    return (
        <div className="profile-page">

            <EmployeeHeader />

            <main className="profile-layout">

                {/* Practice logo section */}
                <aside className="profile-sidebar">
                    <img
                        src={logo}
                        alt="Wake Orthodontics"
                        className="profile-logo"
                    />
                </aside>

                <section className="profile-content">

                    {/* Employee profile header */}
                    <div className="profile-top">
                        <div className="tooth-icon">🦷</div>

                        <div>
                            <h3>{fullName || 'Your Name'}</h3>
                            <p>{employee?.username || 'username'}</p>
                        </div>
                    </div>

                    {/* Error message */}
                    {error && (
                        <p className="error-message">
                            {error}
                        </p>
                    )}

                    <div className="profile-form">

                        {/* Left column */}
                        <div className="profile-column">

                            <div className="profile-row">
                                <span>Name:</span>
                                <input
                                    value={fullName}
                                    readOnly
                                />
                            </div>

                            <div className="profile-row">
                                <span>User Name:</span>
                                <input
                                    value={employee?.username || ''}
                                    readOnly
                                />
                            </div>

                            <div className="profile-row">
                                <span>Email:</span>
                                <input
                                    value={employee?.email || ''}
                                    readOnly
                                />
                            </div>

                            <div className="profile-row">
                                <span>Phone Number:</span>
                                <input
                                    value={formatPhoneNumber(employee?.phoneNumber)}
                                    readOnly
                                />
                            </div>

                            <div className="profile-row">
                                <span>Password:</span>
                                <input
                                    type="password"
                                    value="********"
                                    readOnly
                                />
                            </div>

                        </div>

                        {/* Right column */}
                        <div className="profile-column">

                            <div className="profile-row">
                                <span>Status:</span>
                                <input
                                    value={employee?.status || ''}
                                    readOnly
                                />
                            </div>

                            <div className="profile-row">
                                <span>Hire Date:</span>
                                <input
                                    value={employee?.hireDate || ''}
                                    readOnly
                                />
                            </div>

                            <div className="profile-row">
                                <span>PTO Hours:</span>
                                <input
                                    value={employee?.timeOff === null || employee?.timeOff === undefined ? '' : `${employee.timeOff} hours`}
                                    readOnly
                                />
                            </div>

                            {/* Navigate to profile edit page */}
                            <button
                                className="edit-profile-button"
                                onClick={() => navigate('/employee/profile/edit')}
                            >
                                <IconPencil size={18} />
                            </button>

                        </div>

                    </div>

                </section>

            </main>

            <footer className="page-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default EmployeeProfilePage
