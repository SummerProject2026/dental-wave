import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import '../App.css'
import logo from '../pictures/wake-logo.png'

import {
    loginAPICall,
    storeToken,
    saveLoggedInUser,
    saveLoggedInUserId
} from '../services/AuthService'

/**
 * Login Page
 *
 * Allows a user to log into DentalWave using a username and password.
 *
 * On successful login:
 * - A JWT token is saved
 * - User session information is saved
 * - User is redirected based on role
 */
function LoginPage() {

    /** Stores the username entered by the user. */
    const [username, setUsername] = useState('')

    /** Stores the password entered by the user. */
    const [password, setPassword] = useState('')

    /** Stores an error message if login fails. */
    const [error, setError] = useState('')

    /** Used to redirect the user after login. */
    const navigator = useNavigate()

    /**
     * Handles the login form submission.
     *
     * Steps:
     * 1. Prevent the page from refreshing.
     * 2. Send username and password to the backend.
     * 3. Read the JwtAuthResponse.
     * 4. Store the JWT token and user information.
     * 5. Redirect based on the user's role.
     */
    async function handleLoginForm(e) {

        // Prevents the browser's default form submission behavior
        e.preventDefault()

        // Clears any previous login error message
        setError('')

        try {
            // Sends login credentials to the backend
            const response = await loginAPICall(username, password)

            /**
             * Expected JwtAuthResponse from backend:
             *
             * {
             *   accessToken: String,
             *   tokenType: String,
             *   userId: Long,
             *   username: String,
             *   role: String,
             *   email: String
             * }
             */

                // Creates the full Authorization token, example: "Bearer eyJhbGciOi..."
            const token = response.data.tokenType + ' ' + response.data.accessToken

            // Gets user information from the backend response
            const userId = response.data.userId
            const resolvedUsername = response.data.username
            const role = response.data.role

            // Stores JWT token in localStorage
            storeToken(token)

            // Stores username and role in sessionStorage
            saveLoggedInUser(resolvedUsername, role)

            // Stores user id in sessionStorage for future employee-specific requests
            saveLoggedInUserId(userId)

            // Redirects the user to the correct page based on their role
            if (role === 'ROLE_HR') {
                navigator('/hr/calendar')
            } else if (role === 'ROLE_MANAGER') {
                navigator('/manager/calendar')
            } else if (role === 'ROLE_ADMIN') {
                navigator('/admin')
            } else if (role === 'ROLE_ASSISTANT') {
                navigator('/employee/calendar')
            } else {
                // Fallback route if an unexpected role is returned
                navigator('/calendar')
            }

        } catch (error) {
            // Prints the error to the console for debugging
            console.error(error)

            // Displays a user-friendly error message
            setError('Invalid username or password. Please try again.')
        }
    }

    return (
        <div className="login-page">

            {/* Main content wrapper controls spacing between logo, form, and footer */}
            <div className="login-content">

                {/* Practice logo */}
                <img
                    className="login-logo"
                    src={logo}
                    alt="Wake Orthodontics and Pediatric Dentistry"
                />

                {/* Login form */}
                <form
                    className="login-form"
                    onSubmit={handleLoginForm}
                >

                    {/* Username label and input */}
                    <label className="login-label">
                        Username:
                    </label>

                    <input
                        className="login-input"
                        type="text"
                        placeholder="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />

                    {/* Future enhancement link */}
                    <a className="forgot-link" href="#">
                        Forgot Username?
                    </a>

                    {/* Password label and input */}
                    <label className="login-label">
                        Password:
                    </label>

                    <input
                        className="login-input"
                        type="password"
                        placeholder="••••••••••••"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    {/* Future enhancement link */}
                    <a className="forgot-link" href="#">
                        Forgot Password?
                    </a>

                    {/* Error message displays only if login fails */}
                    {error && (
                        <p className="login-error">
                            {error}
                        </p>
                    )}

                    {/* Submit button */}
                    <button
                        className="login-button"
                        type="submit"
                    >
                        Login
                    </button>

                </form>

                {/* Footer text */}
                <p className="login-footer">
                    © All Rights Reserved
                </p>

            </div>
        </div>
    )
}

export default LoginPage