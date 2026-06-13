import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import { getAllEmployees } from '../services/EmployeeService'

function HREmployeesPage() {
    const navigate = useNavigate()

    const [employees, setEmployees] = useState([])
    const [searchTerm, setSearchTerm] = useState('')

    useEffect(() => {
        loadEmployees()
    }, [])

    function loadEmployees() {
        getAllEmployees()
            .then((response) => {
                setEmployees(response.data)
            })
            .catch((error) => {
                console.error('Error loading employees:', error)
            })
    }

    function getEmployeeName(employee) {
        return `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
    }


    const sortedEmployees = [...employees].sort((a, b) => {
        const nameA = getEmployeeName(a).toLowerCase()
        const nameB = getEmployeeName(b).toLowerCase()
        const search = searchTerm.toLowerCase()

        const aMatches = nameA.includes(search)
        const bMatches = nameB.includes(search)

        if (aMatches && !bMatches) return -1
        if (!aMatches && bMatches) return 1

        return nameA.localeCompare(nameB)
    })

    return (
        <div className="hr-page">
            <HRHeader />

            <main className="hr-employees-content">
                <h1 className="hr-page-title">Employees</h1>

                <div className="employee-search-row">
                    <label>Search:</label>
                    <input
                        type="text"
                        placeholder="Name"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                <section className="employee-table-section">
                    <div className="employee-table-top">
                        <button
                            className="new-employee-btn"
                            onClick={() => navigate('/hr/employees/new')}
                        >
                            + New Employee
                        </button>
                    </div>

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
                        {sortedEmployees.length > 0 ? (
                                sortedEmployees.map((employee) => (
                                <tr key={employee.id}>
                                    <td>{getEmployeeName(employee)}</td>
                                    <td>{employee.position}</td>
                                    <td>{employee.status}</td>
                                    <td className="employee-actions">
                                        <button
                                            type="button"
                                            className="employee-action-btn"
                                            onClick={() => navigate(`/hr/employees/${employee.id}`)}
                                            title="View employee"
                                        >
                                            👁
                                        </button>

                                        <button
                                            type="button"
                                            className="employee-action-btn"
                                            onClick={() => navigate(`/hr/employees/${employee.id}/edit`)}
                                            title="Edit Employee"
                                        >
                                            ✏️
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

export default HREmployeesPage