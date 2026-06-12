import axios from 'axios'

// Base URL for authentication endpoints in the Spring Boot backend
const AUTH_REST_API_BASE_URL = 'http://localhost:8080/api/auth'

// Sends a login request to the backend.
// This should match your LoginDto:
// LoginDto
// - username
// - password
export const loginAPICall = (username, password) =>
    axios.post(AUTH_REST_API_BASE_URL + '/login', {
        username,
        password
    })

// Sends a register request to the backend.
// This should match your RegisterDto.
// Since you are using username, your RegisterDto may include:
// - firstName
// - lastName
// - username
// - email
// - phoneNumber
// - password
export const registerAPICall = (registerObj) =>
    axios.post(AUTH_REST_API_BASE_URL + '/register', registerObj)

// Stores the JWT token in localStorage.
// Example token format: Bearer eyJhbGciOiJIUzI1NiJ9...
// export const storeToken = (token) => {
//     localStorage.setItem('token', token)
// }
export const storeToken = (token) => {
    const cleanToken = token.startsWith('Bearer ')
        ? token.substring(7)
        : token

    console.log('Saving token:', cleanToken)

    localStorage.setItem('token', cleanToken)
}

// Gets the JWT token from localStorage.
export const getToken = () => {
    return localStorage.getItem('token')
}

// Saves the logged-in user's basic session information.
// This lets the frontend know who is currently logged in.
export const saveLoggedInUser = (username, role) => {
    sessionStorage.setItem('authenticatedUser', username)
    sessionStorage.setItem('role', role)
}

// Saves the logged-in user's id.
// This is useful for employee-specific pages like "My Calendar" or "My Requests".
export const saveLoggedInUserId = (userId) => {
    sessionStorage.setItem('userId', userId)
}

// Gets the logged-in user's username.
export const getLoggedInUser = () => {
    return sessionStorage.getItem('authenticatedUser')
}

// Gets the logged-in user's id.
export const getLoggedInUserId = () => {
    return sessionStorage.getItem('userId')
}

// Gets the logged-in user's role.
export const getLoggedInUserRole = () => {
    return sessionStorage.getItem('role')
}

// Checks whether a user is currently logged in.
export const isUserLoggedIn = () => {
    const username = sessionStorage.getItem('authenticatedUser')
    return username !== null
}

// Logs the user out.
// This removes both the JWT token and the session information.
export const logout = () => {
    localStorage.clear()
    sessionStorage.clear()
}

// Checks if the logged-in user is an Admin.
export const isAdminUser = () => {
    const role = sessionStorage.getItem('role')
    return role === 'ROLE_ADMIN'
}

// Checks if the logged-in user is an HR user.
export const isHRUser = () => {
    const role = sessionStorage.getItem('role')
    return role === 'ROLE_HR'
}

// Checks if the logged-in user is a Manager.
export const isManagerUser = () => {
    const role = sessionStorage.getItem('role')
    return role === 'ROLE_MANAGER'
}

// Checks if the logged-in user is an Assistant.
export const isAssistantUser = () => {
    const role = sessionStorage.getItem('role')
    return role === 'ROLE_ASSISTANT'
}

// Creates an authorization header for protected API calls.
// Other service files can use this when calling secured backend endpoints.
export const getAuthHeader = () => {
    const token = getToken()

    console.log('Using token:', token)

    return {
        headers: {
            Authorization: `Bearer ${token}`
        }
    }
}

// Saves the logged-in user's email.
export const saveLoggedInUserEmail = (email) => {
    sessionStorage.setItem('email', email)
}

// Gets the logged-in user's email.
export const getLoggedInUserEmail = () => {
    return sessionStorage.getItem('email')
}