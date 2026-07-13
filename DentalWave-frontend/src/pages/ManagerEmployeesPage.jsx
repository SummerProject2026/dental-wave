import '../App.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import ManagerHeader from '../components/ManagerHeader'
import { getAllEmployees } from '../services/EmployeeService'
import {
    getAllCalendars,
    scheduleEmployeeAcrossCalendar
} from '../services/CalendarService'
import { IconEye } from '../components/Icons'

const OFFICES = [
    { id: 1, name: 'Raleigh' },
    { id: 2, name: 'Garner' },
    { id: 3, name: 'Smithfield' }
]

function ManagerEmployeesPage() {

    const navigate = useNavigate()
    const today = new Date()

    const [employees, setEmployees] = useState([])
    const [calendars, setCalendars] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [activeTab, setActiveTab] = useState('employee')
    const [selectedOfficeId, setSelectedOfficeId] = useState(OFFICES[0].id)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const monthName = today.toLocaleString('default', { month: 'long' })
    const year = today.getFullYear()
    const monthLabel = `${monthName} ${year}`

    useEffect(() => {
        loadEmployees()
        loadCalendars()
    }, [])

    function loadEmployees() {
        getAllEmployees()
            .then((response) => setEmployees(response.data))
            .catch((error) => console.error('Error loading employees:', error))
    }

    function loadCalendars() {
        getAllCalendars()
            .then((response) => setCalendars(response.data || []))
            .catch((error) => console.error('Error loading calendars:', error))
    }

    function getEmployeeName(employee) {
        return `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
    }

    // The active calendar for the currently selected office, current month
    const activeCalendar = calendars.find(
        (cal) => cal.officeId === selectedOfficeId && cal.month === monthLabel
    )

    // Set of employee ids who appear on at least one team in the active calendar
    function getScheduledEmployeeIds() {
        const ids = new Set()
        if (!activeCalendar) return ids

            ;(activeCalendar.schedules || []).forEach((schedule) => {
            if (!schedule.teams) return
            Object.values(schedule.teams).forEach((teamEmployees) => {
                teamEmployees.forEach((emp) => ids.add(emp.id))
            })
        })

        return ids
    }

    const scheduledEmployeeIds = getScheduledEmployeeIds()

    const filteredEmployees = employees.filter((employee) => {
        const name = getEmployeeName(employee).toLowerCase()
        const matchesSearch = name.includes(searchTerm.toLowerCase())

        if (activeTab === 'employee') {
            return matchesSearch && employee.status !== 'INACTIVE'
        } else if (activeTab === 'new') {
            // Active employees not yet scheduled anywhere in the active calendar
            return matchesSearch
                && employee.status !== 'INACTIVE'
                && !scheduledEmployeeIds.has(employee.id)
        } else if (activeTab === 'removed') {
            // Deactivated/terminated employee accounts
            return matchesSearch && employee.status === 'INACTIVE'
        }
        return matchesSearch
    })

    async function handleSchedule(employee) {
        if (!activeCalendar) {
            setError('No calendar exists yet for this office and month. Generate one first.')
            return
        }

        setLoading(true)
        setError('')
        try {
            await scheduleEmployeeAcrossCalendar(activeCalendar.id, employee.id)
            await loadCalendars()
        } catch (err) {
            console.error('Failed to schedule employee', err)
            setError('Failed to schedule employee. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="hr-page">

            <ManagerHeader />

            <main className="hr-employees-content">

                {/* Office selector for the active calendar context */}
                <div className="manager-employee-search">
                    <label>Office:</label>
                    <select
                        value={selectedOfficeId}
                        onChange={(e) => setSelectedOfficeId(Number(e.target.value))}
                    >
                        {OFFICES.map((office) => (
                            <option key={office.id} value={office.id}>{office.name}</option>
                        ))}
                    </select>
                </div>

                {error && <p className="error-message">{error}</p>}

                {/* Tab buttons */}
                <div className="manager-employee-tabs">
                    <button
                        className={`manager-tab-btn tab-employee ${activeTab === 'employee' ? 'active' : ''}`}
                        onClick={() => setActiveTab('employee')}
                    >
                        Employee
                    </button>
                    <button
                        className={`manager-tab-btn tab-new ${activeTab === 'new' ? 'active' : ''}`}
                        onClick={() => setActiveTab('new')}
                    >
                        New Employee
                    </button>
                    <button
                        className={`manager-tab-btn tab-removed ${activeTab === 'removed' ? 'active' : ''}`}
                        onClick={() => setActiveTab('removed')}
                    >
                        Removed Employee
                    </button>
                </div>

                {/* Search */}
                <div className="manager-employee-search">
                    <label>Search:</label>
                    <input
                        type="text"
                        placeholder="Name"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* Table */}
                <section className="employee-table-section">
                    <table className="employee-table">
                        <thead>
                        <tr>
                            <th>Employee</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredEmployees.length > 0 ? (
                            filteredEmployees.map((employee) => (
                                <tr key={employee.id} className="request-row">
                                    <td>{getEmployeeName(employee)}</td>
                                    <td>{employee.position}</td>
                                    <td>{employee.status}</td>
                                    <td>
                                        {activeTab === 'new' ? (
                                            <button
                                                className="edit-schedule-btn"
                                                onClick={() => handleSchedule(employee)}
                                                disabled={loading}
                                            >
                                                Schedule
                                            </button>
                                        ) : (
                                            <button
                                                className="employee-action-btn"
                                                onClick={() => navigate(`/manager/employees/${employee.id}`)}
                                                title="View employee"
                                            >
                                                <IconEye size={18} />
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="4" className="empty-table-message">
                                    No employees found.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </section>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerEmployeesPage