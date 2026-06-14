import './App.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import LoginPage from './pages/LoginPage'
import ManagerCalendarPage from './pages/ManagerCalendarPage'
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

function App() {
    return (
        <BrowserRouter>
            <Routes>

                {/* Authentication */}
                <Route path="/" element={<LoginPage />} />
                <Route path="/login" element={<LoginPage />} />

                {/* HR */}
                <Route path="/hr/calendar" element={<HRCalendarPage />} />

                {/* Manager */}
                <Route path="/manager/calendar" element={<ManagerCalendarPage />} />

                {/* Assistant / Employee */}
                <Route path="/employee/calendar" element={<EmployeeCalendarPage />} />

                {/* Admin */}
                <Route path="/admin" element={<h2>Admin Dashboard</h2>} />

                {/* Fallback calendar route */}
                <Route path="/calendar" element={<h2>Calendar</h2>} />

                {/* Employee Profile/Info */}
                <Route path="/employee/profile" element={<EmployeeProfilePage />} />

                {/* Employee Edit Profile */}
                <Route path="/employee/profile/edit" element={<EmployeeEditProfilePage />} />

                {/* Employee Request list */}
                <Route path="/employee/requests" element={<EmployeeMyRequestsPage />} />

                {/* Single request for employee to review */ }
                <Route path="/employee/requests/:requestId" element={<EmployeeRequestTimeOffPage />} />

                {/* HR page to manage employees */ }
                <Route path="/hr/employees" element={<HREmployeesPage />} />

                {/* HR adds a new employee page */}
                <Route path="/hr/employees/new" element={<HRAddEmployeePage />} />

                {/* HR views aa employee page */}
                <Route path="/hr/employees/:id" element={<HRViewEmployeePage />} />

                {/* HR edits an employee page */}
                <Route path="/hr/employees/:id/edit" element={<HREditEmployeePage />} />

                {/* HR info page */}
                <Route path="/hr/profile" element={<HRProfilePage />} />

                {/* Manager Profile page */}
                <Route path="/manager/profile" element={<ManagerProfilePage />} />

                {/* Employee Request time off page */}
                <Route path="/employee/requests/new" element={<EmployeeRequestTimeOffPage />} />

                {/* HR Requests page */}
                <Route path="/hr/requests" element={<HRRequestsPage />} />

                {/* Manager Approved Requests page */}
                <Route path="/manager/requests" element={<ManagerRequestsPage />} />

                {/* Manager Employees page */}
                <Route path="/manager/employees" element={<ManagerEmployeesPage />} />


            </Routes>
        </BrowserRouter>
    )
}

export default App