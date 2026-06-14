import '../App.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import ManagerHeader from '../components/ManagerHeader'
import { getAllEmployees } from '../services/EmployeeService'

function ManagerEmployeesPage() {

    const navigate = useNavigate()
    const [employees, setEmployees] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [activeTab, setActiveTab] = useState('employee')

    useEffect(() => {
        loadEmployees()
    }, [])

    function loadEmployees() {
        getAllEmployees()
            .then((response) => setEmployees(response.data))
            .catch((error) => console.error('Error loading employees:', error))
    }

    function getEmployeeName(employee) {
        return `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
    }

    const filteredEmployees = employees.filter((employee) => {
        const name = getEmployeeName(employee).toLowerCase()
        const matchesSearch = name.includes(searchTerm.toLowerCase())

        if (activeTab === 'employee') {
            return matchesSearch && employee.status !== 'REMOVED'
        } else if (activeTab === 'new') {
            // New employees — hired in last 30 days
            const hireDate = new Date(employee.hireDate)
            const thirtyDaysAgo = new Date()
            thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
            return matchesSearch && hireDate >= thirtyDaysAgo
        } else if (activeTab === 'removed') {
            return matchesSearch && employee.status === 'REMOVED'
        }
        return matchesSearch
    })

    return (
        <div className="hr-page">

            <ManagerHeader />

            <main className="hr-employees-content">

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
                                        <button
                                            className="employee-action-btn"
                                            onClick={() => navigate(`/manager/employees/${employee.id}`)}
                                            title="View employee"
                                        >
                                            👁
                                        </button>
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