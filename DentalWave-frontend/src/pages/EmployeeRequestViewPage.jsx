import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import EmployeeHeader from '../components/EmployeeHeader'
import {
    getTimeOffRequestById,
    deleteTimeOffRequest
} from '../services/TimeOffRequestService'

function EmployeeRequestViewPage() {
    const navigate = useNavigate()
    const { requestId } = useParams()

    const [request, setRequest] = useState(null)
    const [error, setError] = useState('')

    useEffect(() => {
        loadRequest()
    }, [requestId])

    function loadRequest() {
        getTimeOffRequestById(requestId)
            .then((response) => {
                setRequest(response.data)
            })
            .catch((error) => {
                console.error(error)
                setError('Unable to load request.')
            })
    }

    function handleDelete() {
        if (!window.confirm('Delete this request?')) {
            return
        }

        deleteTimeOffRequest(requestId)
            .then(() => {
                navigate('/employee/requests')
            })
            .catch((error) => {
                console.error(error)
                setError('Unable to delete request.')
            })
    }

    function handleClose() {
        navigate('/employee/requests')
    }

    if (error) {
        return (
            <div>
                <EmployeeHeader />
                <p>{error}</p>
            </div>
        )
    }

    if (!request) {
        return (
            <div>
                <EmployeeHeader />
                <p>Loading...</p>
            </div>
        )
    }

    return (
        <div className="employee-calendar-page">

            <EmployeeHeader />

            <main className="calendar-content">

                <div className="request-view-card">

                    <div className="request-view-header">
                        <span>
                            Status: {request.status}
                        </span>

                        <span>
                            Emergency: {request.emergency ? 'YES' : 'NO'}
                        </span>
                    </div>

                    <hr className="request-divider" />

                    <div className="request-view-body">

                        <div className="request-details">

                            <h4>Requested Dates</h4>

                            <p>
                                Start Date: {request.startDate}
                            </p>

                            <p>
                                End Date: {request.endDate}
                            </p>

                            <br />

                            <h4>Requested Times</h4>

                            <p>
                                Start Time: {request.startTime}
                            </p>

                            <p>
                                End Time: {request.endTime}
                            </p>

                        </div>

                        <div className="request-comment-section">

                            <h4>Reason/Comment</h4>

                            <div className="request-comment-box">
                                {request.comment}
                            </div>

                        </div>

                    </div>

                    <div className="request-footer-info">

                        <span>
                            Submitted: {request.submittedDate}
                        </span>

                        <span>
                            Reviewed By: {request.reviewedBy || 'Not Reviewed'}
                        </span>

                    </div>

                    <div className="request-buttons">

                        <button
                            className="delete-button"
                            onClick={handleDelete}
                        >
                            Delete
                        </button>

                        <button
                            className="ok-button"
                            onClick={handleClose}
                        >
                            OK
                        </button>

                    </div>

                </div>

            </main>

            <footer className="page-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default EmployeeRequestViewPage