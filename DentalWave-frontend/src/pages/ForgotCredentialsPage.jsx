import '../App.css'
import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import logo from '../pictures/wake-logo.png'
import { forgotPassword, forgotUsername } from '../services/AuthService'

/**
 * ForgotCredentialsPage
 *
 * Shared page for both the Forgot Password and Forgot Username flows.
 * The mode is determined by the current route:
 *  - /forgot-password → resets the password and emails a temporary one
 *  - /forgot-username → emails the user their username
 *
 * The user must provide their first name, last name, and email.
 * All three must match their account record before an email is sent.
 */
function ForgotCredentialsPage() {

    const navigate = useNavigate()
    const location = useLocation()

    // Determine mode from the current URL
    const isPasswordMode = location.pathname === '/forgot-password'

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: ''
    })

    const [message, setMessage] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    function handleChange(event) {
        const { name, value } = event.target
        setFormData({
            ...formData,
            [name]: value
        })
    }

    async function handleSubmit(event) {
        event.preventDefault()
        setError('')
        setMessage('')
        setLoading(true)

        try {
            if (isPasswordMode) {
                await forgotPassword(formData)
            } else {
                await forgotUsername(formData)
            }

            setMessage(
                'If the information matches our records, an email has been sent. ' +
                'Please check your inbox.'
            )
        } catch (err) {
            console.error('Recovery request failed:', err)
            setError('Something went wrong. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-page">

            <main className="login-content">

                <img
                    src={logo}
                    alt="Wake Orthodontics"
                    className="login-logo"
                />

                <h2 className="forgot-title">
                    {isPasswordMode ? 'Forgot Password' : 'Forgot Username'}
                </h2>

                <p className="forgot-subtitle">
                    {isPasswordMode
                        ? 'Enter your details below. If they match our records, a temporary password will be emailed to you.'
                        : 'Enter your details below. If they match our records, your username will be emailed to you.'}
                </p>

                <form className="login-form" onSubmit={handleSubmit}>

                    <label className="login-label">First Name:</label>
                    <input
                        className="login-input"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleChange}
                        required
                    />

                    <label className="login-label">Last Name:</label>
                    <input
                        className="login-input"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        required
                    />

                    <label className="login-label">Email:</label>
                    <input
                        className="login-input"
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />

                    {message && <p className="success-message">{message}</p>}
                    {error && <p className="error-message">{error}</p>}

                    <button type="submit" className="login-button" disabled={loading}>
                        {loading ? 'Sending...' : 'Submit'}
                    </button>

                    <button
                        type="button"
                        className="back-to-login-btn"
                        onClick={() => navigate('/login')}
                    >
                        Back to Login
                    </button>

                </form>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ForgotCredentialsPage