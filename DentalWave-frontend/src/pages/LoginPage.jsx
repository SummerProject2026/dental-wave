import { useState } from 'react'
import { useNavigate, Link, useLocation } from 'react-router-dom'

import '../App.css'
import logo from '../pictures/wake-logo.png'

import {
    loginAPICall,
    storeToken,
    saveLoggedInUser,
    saveLoggedInUserId,
    saveLoggedInUserName
} from '../services/AuthService'

/**
 * Login Page
 *
 * Allows a user to log into DentalWave using a username and password.
 *
 * On successful login:
 * - JWT token is saved
 * - Username, role, userId, and employeeId are saved
 * - User is redirected based on role
 */
function LoginPage() {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [showPassword, setShowPassword] = useState(false)
    const location = useLocation()
    const [error, setError] = useState(
        location.state?.sessionExpired
            ? 'Your session expired. Please log in again.'
            : ''
    )

    const navigator = useNavigate()

    async function handleLoginForm(e) {
        e.preventDefault()
        setError('')

        try {
            const response = await loginAPICall(username, password)

            const token = response.data.tokenType + ' ' + response.data.accessToken

            const userId = response.data.userId
            const employeeId = response.data.employeeId
            const resolvedUsername = response.data.username
            const role = response.data.role

            // Store JWT token
            storeToken(token)

            // Store username and role
            saveLoggedInUser(resolvedUsername, role)

            // Store user id
            saveLoggedInUserId(userId)

            // Store first and last name for display (e.g. avatar initials)
            saveLoggedInUserName(response.data.firstName, response.data.lastName)

            // Store employee id for employee-specific pages
            sessionStorage.setItem('employeeId', employeeId)

            if (role === 'ROLE_HR') {
                navigator('/hr/calendar')
            } else if (role === 'ROLE_MANAGER') {
                navigator('/manager/calendar')
            } else if (role === 'ROLE_ADMIN') {
                navigator('/admin')
            } else if (role === 'ROLE_ASSISTANT') {
                navigator('/employee/calendar')
            } else {
                navigator('/calendar')
            }

        } catch (error) {
            console.error(error)
            setError('Invalid username or password. Please try again.')
        }
    }

    return (
        <div className="login-page">
            <div className="login-content">
                <img
                    className="login-logo"
                    src={logo}
                    alt="Wake Orthodontics and Pediatric Dentistry"
                />

                <form className="login-form" onSubmit={handleLoginForm}>
                    <label className="login-label">Username:</label>

                    <input
                        className="login-input"
                        type="text"
                        placeholder="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />

                    <Link className="forgot-link" to="/forgot-username">
                        Forgot Username?
                    </Link>

                    <label className="login-label">Password:</label>

                    <div className="password-input-row">
                        <input
                            className="login-input"
                            type={showPassword ? 'text' : 'password'}
                            placeholder="••••••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <button
                            type="button"
                            className="password-eye-button"
                            onClick={() => setShowPassword((prev) => !prev)}
                            aria-label={showPassword ? 'Hide password' : 'Show password'}
                        >
                            {showPassword ? '🙈' : '👁️'}
                        </button>
                    </div>

                    <Link className="forgot-link" to="/forgot-password">
                        Forgot Password?
                    </Link>

                    {error && (
                        <p className="login-error">
                            {error}
                        </p>
                    )}

                    <button className="login-button" type="submit">
                        Login
                    </button>
                </form>

                <p className="login-footer">
                    © All Rights Reserved
                </p>
            </div>
        </div>
    )
}

export default LoginPage
