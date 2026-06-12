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
        if (employee.name) {
            return employee.name
        }

        return `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
    }

    const filteredEmployees = employees.filter((employee) =>
        getEmployeeName(employee).toLowerCase().includes(searchTerm.toLowerCase())
    )

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
                        {filteredEmployees.map((employee) => (
                            <tr key={employee.id}>
                                <td>{getEmployeeName(employee)}</td>
                                <td>{employee.role}</td>
                                <td>{employee.status}</td>
                                <td className="employee-actions">
                                    <button onClick={() => navigate(`/hr/employees/${employee.id}/edit`)}>
                                        ✎
                                    </button>

                                    <button onClick={() => navigate(`/hr/employees/${employee.id}`)}>
                                        👁
                                    </button>
                                </td>
                            </tr>
                        ))}

                        {Array.from({
                            length: Math.max(5 - filteredEmployees.length, 0)
                        }).map((_, index) => (
                            <tr key={`empty-${index}`}>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </section>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HREmployeesPage