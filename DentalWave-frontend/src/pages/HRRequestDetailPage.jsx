import '../App.css'
import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import HRHeader from '../components/HRHeader'
import {
    getTimeOffRequestById,
    approveTimeOffRequest,
    denyTimeOffRequest
} from '../services/TimeOffRequestService'
import { getLoggedInUserId } from '../services/AuthService'

function HRRequestDetailPage() {
    const { id } = useParams()
    const navigate = useNavigate()

    const [request, setRequest] = useState(null)
    const [reviewComment, setReviewComment] = useState('')
    const [error, setError] = useState('')
    const [actionLoading, setActionLoading] = useState(false)

    useEffect(() => {
        getTimeOffRequestById(id)
            .then((response) => {
                setRequest(response.data)
            })
            .catch((err) => {
                console.error('Failed to load request', err)
                setError('This request is no longer available.')
            })
    }, [id])

    function formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return ''
        const date = new Date(dateTimeStr)
        if (isNaN(date.getTime())) return dateTimeStr
        return date.toLocaleString()
    }

    async function handleApprove() {
        setActionLoading(true)
        setError('')
        try {
            const reviewedById = getLoggedInUserId()
            const response = await approveTimeOffRequest(id, reviewedById, reviewComment)
            setRequest(response.data)
        } catch (err) {
            console.error('Failed to approve request', err)
            setError('Unable to approve this request. Please try again.')
        } finally {
            setActionLoading(false)
        }
    }

    async function handleReject() {
        setActionLoading(true)
        setError('')
        try {
            const reviewedById = getLoggedInUserId()
            const response = await denyTimeOffRequest(id, reviewedById, reviewComment)
            setRequest(response.data)
        } catch (err) {
            console.error('Failed to deny request', err)
            setError('Unable to deny this request. Please try again.')
        } finally {
            setActionLoading(false)
        }
    }

    if (error && !request) {
        return (
            <div className="hr-page">
                <HRHeader />
                <main className="hr-request-detail-content">
                    <p className="error-message">{error}</p>
                    <button className="cancel-request-btn" onClick={() => navigate('/hr/requests')}>
                        Back to Requests
                    </button>
                </main>
                <footer className="page-footer">© All Rights Reserved</footer>
            </div>
        )
    }

    if (!request) {
        return (
            <div className="hr-page">
                <HRHeader />
                <main className="hr-request-detail-content">
                    <p>Loading request...</p>
                </main>
            </div>
        )
    }

    const employeeName = request.employeeName ||
        `${request.employeeFirstName || ''} ${request.employeeLastName || ''}`.trim() ||
        'Unknown Employee'

    const isPending = request.status?.toUpperCase() === 'PENDING'

    return (
        <div className="hr-page">
            <HRHeader />

            <main className="hr-request-detail-content">
                <div className="request-detail-card">

                    <div className="request-detail-header">
                        <div>
                            <p><strong>Employee:</strong> {employeeName}</p>
                            <p><strong>Status:</strong> {request.status}</p>
                        </div>
                        {request.emergency && (
                            <span className="emergency-badge">Emergency: YES [!]</span>
                        )}
                    </div>

                    <hr />

                    <div className="request-detail-section">
                        <h3>Requested Dates</h3>
                        <p>Start Date: {request.startDate}</p>
                        <p>End Date: {request.endDate}</p>
                    </div>

                    {(request.startTime || request.endTime) && (
                        <div className="request-detail-section">
                            <h3>Requested Times</h3>
                            <p>Start Time: {request.startTime}</p>
                            <p>End Time: {request.endTime}</p>
                        </div>
                    )}

                    <div className="request-detail-section">
                        <h3>Reason/Comment:</h3>
                        <textarea
                            className="reason-textarea"
                            value={reviewComment}
                            onChange={(e) => setReviewComment(e.target.value)}
                            placeholder={request.reason || 'No reason provided'}
                            disabled={!isPending}
                        />
                    </div>

                    {error && <p className="error-message">{error}</p>}

                    <div className="request-detail-footer">
                        <span>Submitted: {formatDateTime(request.submittedAt)}</span>
                        <span>
                            Reviewed By: {request.reviewedByName || 'Not yet Reviewed'}
                        </span>
                    </div>

                    <div className="request-detail-actions">
                        {isPending ? (
                            <>
                                <button
                                    className="approve-request-btn"
                                    onClick={handleApprove}
                                    disabled={actionLoading}
                                >
                                    Approve
                                </button>
                                <button
                                    className="reject-request-btn"
                                    onClick={handleReject}
                                    disabled={actionLoading}
                                >
                                    Reject
                                </button>
                            </>
                        ) : null}
                        <button
                            className="cancel-request-btn"
                            onClick={() => navigate('/hr/requests')}
                        >
                            {isPending ? 'Cancel' : 'Back'}
                        </button>
                    </div>

                </div>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default HRRequestDetailPage