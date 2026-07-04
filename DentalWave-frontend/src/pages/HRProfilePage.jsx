import '../App.css'
import logo from '../pictures/wake-logo.png'
import HRHeader from '../components/HRHeader'
import { useEffect, useState } from 'react'
import { getCurrentUser, updateCurrentUser } from '../services/UserService'

function HRProfilePage() {

    const [isEditing, setIsEditing] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [loading, setLoading] = useState(false)
    const [form, setForm] = useState({
        id: '',
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        phoneNumber: '',
        status: '',
        hireDate: '',
        pto: '',
        roles: []
    })

    useEffect(() => {
        loadProfile()
    }, [])

    function safeValue(value) {
        return value ?? ''
    }

    function mapUserToForm(user) {
        return {
            id: safeValue(user.id),
            firstName: safeValue(user.firstName),
            lastName: safeValue(user.lastName),
            username: safeValue(user.username),
            email: safeValue(user.email),
            phoneNumber: safeValue(user.phoneNumber),
            status: user.enabled === null || user.enabled === undefined
                ? ''
                : user.enabled ? 'Active' : 'Disabled',
            hireDate: '',
            pto: '',
            roles: user.roles || []
        }
    }

    function loadProfile() {
        setLoading(true)
        setError('')

        getCurrentUser()
            .then((response) => {
                setForm(mapUserToForm(response.data || {}))
            })
            .catch((err) => {
                console.error('Unable to load HR profile', err)
                setError('Unable to load profile information.')
            })
            .finally(() => setLoading(false))
    }

    function handleChange(e) {
        setForm({ ...form, [e.target.name]: e.target.value })
    }

    function handleCancel() {
        setIsEditing(false)
        setSuccess('')
        loadProfile()
    }

    function handleSave() {
        setLoading(true)
        setError('')
        setSuccess('')

        updateCurrentUser({
            id: form.id || null,
            firstName: form.firstName,
            lastName: form.lastName,
            username: form.username,
            email: form.email,
            phoneNumber: form.phoneNumber,
            roles: form.roles
        })
            .then((response) => {
                setForm(mapUserToForm(response.data || {}))
                setIsEditing(false)
                setSuccess('Profile updated.')
            })
            .catch((err) => {
                console.error('Unable to update HR profile', err)
                setError('Unable to update profile information.')
            })
            .finally(() => setLoading(false))
    }

    const fullName = `${form.firstName} ${form.lastName}`.trim()

    return (
        <div className="profile-page">

            <HRHeader />

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
                            <p>{form.username || 'username'}</p>
                        </div>
                    </div>

                    {error && <p className="error-message">{error}</p>}
                    {success && <p className="success-message">{success}</p>}

                    <div className="profile-form">

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>First Name:</span>
                                <input
                                    name="firstName"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Last Name:</span>
                                <input
                                    name="lastName"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>User Name:</span>
                                <input
                                    name="username"
                                    value={form.username}
                                    readOnly
                                />
                            </div>
                            <div className="profile-row">
                                <span>Email:</span>
                                <input
                                    name="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Phone number:</span>
                                <input
                                    name="phoneNumber"
                                    value={form.phoneNumber}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Password:</span>
                                <input
                                    name="password"
                                    type="password"
                                    value=""
                                    placeholder="Password is not shown"
                                    readOnly
                                />
                            </div>
                        </div>

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Status:</span>
                                <input
                                    name="status"
                                    value={form.status}
                                    readOnly
                                />
                            </div>
                            <div className="profile-row">
                                <span>Hire Date:</span>
                                <input
                                    name="hireDate"
                                    value={form.hireDate}
                                    readOnly
                                />
                            </div>
                            <div className="profile-row">
                                <span>PTO:</span>
                                <input
                                    name="pto"
                                    value={form.pto}
                                    readOnly
                                />
                            </div>

                            {isEditing ? (
                                <div className="profile-action-row">
                                    <button
                                        className="save-profile-button"
                                        onClick={handleSave}
                                        disabled={loading}
                                    >
                                        Save
                                    </button>
                                    <button
                                        className="cancel-profile-button"
                                        onClick={handleCancel}
                                        disabled={loading}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            ) : (
                                <button
                                    className="edit-profile-button"
                                    onClick={() => {
                                        setSuccess('')
                                        setError('')
                                        setIsEditing(true)
                                    }}
                                    disabled={loading}
                                >
                                    ✏️
                                </button>
                            )}
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

export default HRProfilePage
