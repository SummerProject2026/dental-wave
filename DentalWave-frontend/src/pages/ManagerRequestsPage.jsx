import '../App.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import ManagerHeader from '../components/ManagerHeader'

function ManagerRequestsPage() {

    const navigate = useNavigate()
    const [requests, setRequests] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [filterBy, setFilterBy] = useState('all')

    // TODO: replace with API call
    // useEffect(() => {
    //     getAllApprovedRequests().then(r => setRequests(r.data))
    // }, [])

    const filteredRequests = requests.filter((req) => {
        const name = `${req.firstName || ''} ${req.lastName || ''}`.toLowerCase()
        const matchesSearch = name.includes(searchTerm.toLowerCase())
        const matchesFilter =
            filterBy === 'all' ||
            (filterBy === 'approved' && req.status?.toLowerCase() === 'approved') ||
            (filterBy === 'pending' && req.status?.toLowerCase() === 'pending') ||
            (filterBy === 'denied' && req.status?.toLowerCase() === 'denied') ||
            (filterBy === 'emergency' && req.emergency)
        return matchesSearch && matchesFilter
    })

    return (
        <div className="hr-page">

            <ManagerHeader />

            <main className="hr-requests-content">

                <div className="hr-requests-search-row">
                    <label>Search:</label>
                    <input
                        type="text"
                        placeholder="Name"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />

                    <label>Filter By:</label>
                    <select
                        value={filterBy}
                        onChange={(e) => setFilterBy(e.target.value)}
                    >
                        <option value="all">Status</option>
                        <option value="approved">Approved</option>
                        <option value="pending">Pending</option>
                        <option value="denied">Denied</option>
                        <option value="emergency">Emergency</option>
                    </select>
                </div>

                <section className="hr-requests-table-section">
                    <table className="employee-table">
                        <thead>
                        <tr>
                            <th>Employee</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Emergency</th>
                            <th>Date</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredRequests.length > 0 ? (
                            filteredRequests.map((req) => (
                                <tr key={req.id} className="request-row">
                                    <td>{req.firstName} {req.lastName}</td>
                                    <td>{req.role}</td>
                                    <td>{req.status}</td>
                                    <td>{req.emergency ? 'YES' : 'NO'}</td>
                                    <td>{req.startDate}</td>
                                    <td>
                                        <button
                                            className="edit-schedule-btn"
                                            onClick={() => navigate(`/manager/requests/${req.id}/edit`)}
                                        >
                                            Edit Schedule
                                        </button>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6" className="empty-table-message">
                                    No requests found.
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

export default ManagerRequestsPage