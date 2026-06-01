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
            </Routes>
        </BrowserRouter>
    )
}

export default App