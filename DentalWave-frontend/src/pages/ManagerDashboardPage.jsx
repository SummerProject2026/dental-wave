import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ManagerHeader from '../components/ManagerHeader'
import { getAllCalendars } from '../services/CalendarService'
import { getResources } from '../services/ManagerSchedulerService'

export default function ManagerDashboardPage() {
  const [summary, setSummary] = useState({ doctors: 0, assistants: 0, status: 'Not created' })
  const month = new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
  useEffect(() => { Promise.all([getResources('DOCTOR'), getResources('ASSISTANT'), getAllCalendars()]).then(([d,a,c]) => { const current=(c.data||[]).filter(x=>x.month===month); setSummary({doctors:d.data.filter(x=>x.active).length, assistants:a.data.filter(x=>x.active).length, status:current.some(x=>x.published)?'Finalized':current.length?'Draft':'Not created'}) }).catch(()=>{}) }, [month])
  const actions = [
    { label: 'Create Monthly Schedule', description: 'Choose a month and generate an editable draft', to: '/manager/calendar/new', primary: true },
    { label: 'Manage Doctors', to: '/manager/doctors' },
    { label: 'Manage Assistants', to: '/manager/assistants' },
    { label: 'View Monthly Schedule', to: '/manager/schedule' }
  ]
  return <div className="calendar-page"><ManagerHeader/><main className="lite-shell"><section className="lite-hero"><p className="eyebrow">Manager Scheduler</p><h1>{month}</h1><p>Build, review, and publish the monthly office schedule.</p><span className="status-pill">{summary.status}</span></section><section className="summary-grid dashboard-summary"><article><strong>{summary.doctors}</strong><span>Active doctors</span></article><article><strong>{summary.assistants}</strong><span>Active assistants</span></article><article><strong>{summary.status}</strong><span>{month} schedule</span></article></section><section className="dashboard-actions"><div className="section-heading"><h2>Quick actions</h2></div><div className="action-grid dashboard-action-grid">{actions.map(action=><Link className={action.primary?'action-card primary':'action-card'} to={action.to} key={action.to}><strong>{action.label}</strong>{action.description&&<small>{action.description}</small>}</Link>)}</div></section></main></div>
}
