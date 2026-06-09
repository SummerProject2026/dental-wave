import './App.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import LoginPage from './pages/LoginPage'
import ManagerCalendarPage from './pages/ManagerCalendarPage'
import EmployeeCalendarPage from './pages/EmployeeCalendarPage'
import EmployeeProfilePage from './pages/EmployeeProfilePage.jsx'
import EmployeeEditProfilePage from './pages/EmployeeEditProfilePage.jsx'

function App() {
    return (
        <BrowserRouter>
            <Routes>

                {/* Authentication */}
                <Route path="/" element={<LoginPage />} />
                <Route path="/login" element={<LoginPage />} />

                {/* HR */}
                <Route path="/hr/calendar" element={<h2>HR Calendar</h2>} />

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

            </Routes>
        </BrowserRouter>
    )
}

export default App