import './App.css'
import axios from 'axios'
import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import ForgotCredentialsPage from './pages/ForgotCredentialsPage'
import ManagerCalendarPage from './pages/ManagerCalendarPage'
import ManagerCalendarOverviewPage from './pages/ManagerCalendarOverviewPage'
import ManagerEditCalendarPage from './pages/ManagerEditCalendarPage'
import ManagerNewCalendarPage from './pages/ManagerNewCalendarPage'
import ManagerDashboardPage from './pages/ManagerDashboardPage'
import ManagerResourcesPage from './pages/ManagerResourcesPage'
import ManagerTeamsPage from './pages/ManagerTeamsPage'
import DoctorSchedulePatternsPage from './pages/DoctorSchedulePatternsPage'
import { getAuthHeader, getLoggedInUserRole, getToken, logout, saveLoggedInUser, saveLoggedInUserId, saveLoggedInUserName } from './services/AuthService'
import { apiUrl } from './services/apiConfig'

function ProtectedRoute({ children }) {
  const location = useLocation()
  const token = getToken()
  const [role, setRole] = useState(getLoggedInUserRole())
  const [loading, setLoading] = useState(Boolean(token) && !role)
  useEffect(() => {
    if (!token || role) return
    axios.get(apiUrl('/api/users/me'), getAuthHeader()).then(response => {
      const user=response.data||{}; const hydratedRole=Array.isArray(user.roles)?user.roles[0]:null
      if(hydratedRole){saveLoggedInUser(user.username,hydratedRole);saveLoggedInUserId(user.id);saveLoggedInUserName(user.firstName,user.lastName);setRole(hydratedRole)}
    }).catch(()=>{logout();setRole(null)}).finally(()=>setLoading(false))
  }, [token, role])
  if(!token) return <Navigate to="/login" replace state={{from:location}}/>
  if(loading) return <main className="lite-shell">Loading secure manager workspace…</main>
  if(role!=='ROLE_MANAGER'&&role!=='ROLE_ADMIN') return <Navigate to="/login" replace state={{unsupportedRole:true}}/>
  return children
}

const Secure=({children})=><ProtectedRoute>{children}</ProtectedRoute>
export default function App(){return <BrowserRouter><Routes>
  <Route path="/" element={<Navigate to="/manager/dashboard" replace/>}/><Route path="/login" element={<LoginPage/>}/>
  <Route path="/forgot-password" element={<ForgotCredentialsPage/>}/><Route path="/forgot-username" element={<ForgotCredentialsPage/>}/>
  <Route path="/manager/dashboard" element={<Secure><ManagerDashboardPage/></Secure>}/>
  <Route path="/manager/schedule" element={<Secure><ManagerCalendarPage/></Secure>}/>
  <Route path="/manager/calendar" element={<Secure><ManagerCalendarOverviewPage/></Secure>}/>
  <Route path="/manager/calendar/build" element={<Navigate to="/manager/schedule" replace/>}/>
  <Route path="/manager/calendar/new" element={<Secure><ManagerNewCalendarPage/></Secure>}/>
  <Route path="/manager/calendar/:id/edit" element={<Secure><ManagerEditCalendarPage/></Secure>}/>
  <Route path="/manager/teams" element={<Secure><ManagerTeamsPage/></Secure>}/>
  <Route path="/manager/doctors" element={<Secure><ManagerResourcesPage type="DOCTOR"/></Secure>}/>
  <Route path="/manager/doctor-patterns" element={<Secure><DoctorSchedulePatternsPage/></Secure>}/>
  <Route path="/manager/assistants" element={<Secure><ManagerResourcesPage type="ASSISTANT"/></Secure>}/>
  <Route path="/manager/print" element={<Secure><ManagerCalendarOverviewPage printMode/></Secure>}/>
  <Route path="*" element={<Navigate to="/manager/dashboard" replace/>}/>
</Routes></BrowserRouter>}
