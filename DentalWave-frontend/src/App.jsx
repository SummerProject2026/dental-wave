// Main application component.
// This component serves as the root of the DentalWave frontend.
// Future routing and page navigation will be added here.

import './App.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import LoginPage from './pages/LoginPage'

function App() {

    return (
        <BrowserRouter>
            <Routes>
                <Route path='/' element={<LoginPage />} />
                <Route path="/employee/calendar" element={<h2>Employee Calendar</h2>} />
                <Route path="/hr/calendar" element={<h2>HR Calendar</h2>} />
                <Route path="/manager/calendar" element={<h2>Manager Calendar</h2>} />
                <Route path="/admin" element={<h2>Admin Dashboard</h2>} />
                <Route path="/calendar" element={<h2>Calendar</h2>} />
            </Routes>
        </BrowserRouter>
    )
}

export default App