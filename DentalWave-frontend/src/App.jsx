import './App.css'
import axios from 'axios'
import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'

import LoginPage from './pages/LoginPage'
import ManagerCalendarPage from './pages/ManagerCalendarPage'
import ManagerCalendarOverviewPage from './pages/ManagerCalendarOverviewPage.jsx'
import ManagerEditCalendarPage from './pages/ManagerEditCalendarPage.jsx'
import ManagerNewCalendarPage from './pages/ManagerNewCalendarPage.jsx'
import EmployeeCalendarPage from './pages/EmployeeCalendarPage'
import EmployeeProfilePage from './pages/EmployeeProfilePage.jsx'
import EmployeeEditProfilePage from './pages/EmployeeEditProfilePage.jsx'
import EmployeeMyRequestsPage from './pages/EmployeeMyRequestsPage.jsx'
import EmployeeRequestTimeOffPage from './pages/EmployeeRequestTimeOffPage.jsx'
import HREmployeesPage from './pages/HREmployeesPage.jsx'
import HRAddEmployeePage from './pages/HRAddEmployeePage.jsx'
import HRViewEmployeePage from './pages/HRViewEmployeePage.jsx'
import HREditEmployeePage from './pages/HREditEmployeePage.jsx'
import HRCalendarPage from './pages/HRCalendarPage.jsx'
import ManagerProfilePage from "./pages/ManagerProfilePage.jsx"
import HRProfilePage from './pages/HRProfilePage'
import HRRequestsPage from './pages/HRRequestsPage'
import ManagerRequestsPage from './pages/ManagerRequestsPage'
import ManagerEmployeesPage from './pages/ManagerEmployeesPage'
import EmployeeRequestTimeOffEditPage from './pages/EmployeeRequestTimeOffEditPage'
import HRRequestDetailPage from './pages/HRRequestDetailPage.jsx'
import ForgotCredentialsPage from './pages/ForgotCredentialsPage'
import {
    getAuthHeader,
    getLoggedInUserRole,
    getToken,
    saveLoggedInUser,
    saveLoggedInUserId,
    saveLoggedInUserName
} from './services/AuthService'

function ProtectedRoute({ allowedRoles, children }) {
    const location = useLocation()
    const token = getToken()
    const [role, setRole] = useState(getLoggedInUserRole())
    const [loading, setLoading] = useState(Boolean(token) && !role)

    useEffect(() => {
        if (!token || role) {
            setLoading(false)
            return
        }

        axios.get('http://localhost:8080/api/users/me', getAuthHeader())
            .then((response) => {
                const user = response.data || {}
                const hydratedRole = Array.isArray(user.roles) ? user.roles[0] : null
                if (hydratedRole) {
                    saveLoggedInUser(user.username, hydratedRole)
                    saveLoggedInUserId(user.id)
                    saveLoggedInUserName(user.firstName, user.lastName)
                    if (user.employeeId) {
                        sessionStorage.setItem('employeeId', user.employeeId)
                    }
                    setRole(hydratedRole)
                }
            })
            .catch(() => setRole(null))
            .finally(() => setLoading(false))
    }, [token, role])

    if (!token) {
        return <Navigate to="/login" replace state={{ from: location }} />
    }

    if (loading) {
        return null
    }

    if (role === 'ROLE_ADMIN' || allowedRoles.includes(role)) {
        return children
    }

    return <Navigate to="/unauthorized" replace />
}

function UnauthorizedPage() {
    return (
        <div className="calendar-page">
            <main className="build-page-layout" style={{ justifyContent: 'center', minHeight: '70vh' }}>
                <section className="calendar-card" style={{ maxWidth: '620px', textAlign: 'center' }}>
                    <h1 className="manager-calendar-title">Unauthorized</h1>
                    <p>You are not authorized to view this page.</p>
                    <a className="request-timeoff-btn" href="/login">Return to Login</a>
                </section>
            </main>
        </div>
    )
}

const HR_ROLES = ['ROLE_HR']
const MANAGER_ROLES = ['ROLE_MANAGER']
const ASSISTANT_ROLES = ['ROLE_ASSISTANT']
const STAFF_ROLES = ['ROLE_ASSISTANT', 'ROLE_HR', 'ROLE_MANAGER']

function App() {
    return (
        <BrowserRouter>
            <Routes>

                {/* Authentication */}
                <Route path="/" element={<LoginPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/unauthorized" element={<UnauthorizedPage />} />

                {/* HR */}
                <Route path="/hr/calendar" element={<ProtectedRoute allowedRoles={HR_ROLES}><HRCalendarPage /></ProtectedRoute>} />

                {/* Manager */}
                <Route path="/manager/calendar" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerCalendarOverviewPage /></ProtectedRoute>} />
                <Route path="/manager/calendar/build" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerCalendarPage /></ProtectedRoute>} />
                <Route path="/manager/calendar/new" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerNewCalendarPage /></ProtectedRoute>} />
                <Route path="/manager/calendar/:id/edit" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerEditCalendarPage /></ProtectedRoute>} />

                {/* Assistant / Employee */}
                <Route path="/employee/calendar" element={<ProtectedRoute allowedRoles={ASSISTANT_ROLES}><EmployeeCalendarPage /></ProtectedRoute>} />

                {/* Admin */}
                <Route path="/admin" element={<ProtectedRoute allowedRoles={[]}><h2>Admin Dashboard</h2></ProtectedRoute>} />

                {/* Fallback calendar route */}
                <Route path="/calendar" element={<ProtectedRoute allowedRoles={STAFF_ROLES}><h2>Calendar</h2></ProtectedRoute>} />

                {/* Employee Profile/Info */}
                <Route path="/employee/profile" element={<ProtectedRoute allowedRoles={ASSISTANT_ROLES}><EmployeeProfilePage /></ProtectedRoute>} />

                {/* Employee Edit Profile */}
                <Route path="/employee/profile/edit" element={<ProtectedRoute allowedRoles={ASSISTANT_ROLES}><EmployeeEditProfilePage /></ProtectedRoute>} />

                {/* Employee Request list */}
                <Route path="/employee/requests" element={<ProtectedRoute allowedRoles={ASSISTANT_ROLES}><EmployeeMyRequestsPage /></ProtectedRoute>} />

                {/* HR page to manage employees */ }
                <Route path="/hr/employees" element={<ProtectedRoute allowedRoles={HR_ROLES}><HREmployeesPage /></ProtectedRoute>} />

                {/* HR adds a new employee page */}
                <Route path="/hr/employees/new" element={<ProtectedRoute allowedRoles={HR_ROLES}><HRAddEmployeePage /></ProtectedRoute>} />

                {/* HR views aa employee page */}
                <Route path="/hr/employees/:id" element={<ProtectedRoute allowedRoles={HR_ROLES}><HRViewEmployeePage /></ProtectedRoute>} />

                {/* HR edits an employee page */}
                <Route path="/hr/employees/:id/edit" element={<ProtectedRoute allowedRoles={HR_ROLES}><HREditEmployeePage /></ProtectedRoute>} />
                <Route path="/hr/employees/edit/:id" element={<ProtectedRoute allowedRoles={HR_ROLES}><HREditEmployeePage /></ProtectedRoute>} />

                {/* HR info page */}
                <Route path="/hr/profile" element={<ProtectedRoute allowedRoles={HR_ROLES}><HRProfilePage /></ProtectedRoute>} />

                {/* Manager Profile page */}
                <Route path="/manager/profile" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerProfilePage /></ProtectedRoute>} />

                {/* Employee Request time off page */}
                <Route path="/employee/requests/new" element={<ProtectedRoute allowedRoles={ASSISTANT_ROLES}><EmployeeRequestTimeOffPage /></ProtectedRoute>} />

                {/* HR Requests page */}
                <Route path="/hr/requests" element={<ProtectedRoute allowedRoles={HR_ROLES}><HRRequestsPage /></ProtectedRoute>} />

                {/* Manager Approved Requests page */}
                <Route path="/manager/requests" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerRequestsPage /></ProtectedRoute>} />

                {/* Manager Employees page */}
                <Route path="/manager/employees" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><ManagerEmployeesPage /></ProtectedRoute>} />
                <Route path="/manager/employees/:id" element={<ProtectedRoute allowedRoles={MANAGER_ROLES}><HRViewEmployeePage /></ProtectedRoute>} />

                {/* Edit the Request page */}
                <Route path="/employee/requests/:id" element={<ProtectedRoute allowedRoles={ASSISTANT_ROLES}><EmployeeRequestTimeOffEditPage /></ProtectedRoute>} />

                <Route path="/hr/requests/:id" element={<ProtectedRoute allowedRoles={HR_ROLES}><HRRequestDetailPage /></ProtectedRoute>} />

                <Route path="/forgot-password" element={<ForgotCredentialsPage />} />
                <Route path="/forgot-username" element={<ForgotCredentialsPage />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
