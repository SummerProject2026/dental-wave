import '../App.css'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import EmployeeHeader from '../components/EmployeeHeader'
import {
    getTimeOffRequestById,
    updateTimeOffRequest,
    deleteTimeOffRequest
} from '../services/TimeOffRequestService'

function EmployeeRequestTimeOffEditPage() {
    const navigate = useNavigate()
    const { id } = useParams()
    const today = new Date()

    const [requestType, setRequestType] = useState('timeoff')
    const [fromDate, setFromDate] = useState('')
    const [fromTime, setFromTime] = useState('')
    const [toDate, setToDate] = useState('')
    const [toTime, setToTime] = useState('')
    const [reason, setReason] = useState('')
    const [status, setStatus] = useState('')
    const [employeeId, setEmployeeId] = useState(null)
    const [errorMessage, setErrorMessage] = useState('')

    const [calDate, setCalDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedDay, setSelectedDay] = useState(null)

    const canEdit = status === 'PENDING'

    const monthName = calDate.toLocaleString('default', { month: 'long' })
    const year = calDate.getFullYear()
    const firstDay = calDate.getDay()
    const daysInMonth = new Date(calDate.getFullYear(), calDate.getMonth() + 1, 0).getDate()

    const cells = []
    for (let i = 0; i < firstDay; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    useEffect(() => {
        getTimeOffRequestById(id)
            .then((response) => {
                const request = response.data

                setEmployeeId(request.employeeId)
                setFromDate(request.startDate || '')
                setToDate(request.endDate || '')
                setFromTime(request.startTime || '')
                setToTime(request.endTime || '')
                setReason(request.reason || '')
                setStatus(request.status || '')
                setRequestType(request.emergency ? 'emergency' : 'timeoff')

                if (request.startDate) {
                    const [year, month, day] = request.startDate.split('-').map(Number)
                    setCalDate(new Date(year, month - 1, 1))
                    setSelectedDay(day)
                }
            })
            .catch((error) => {
                console.error('Failed to load request:', error)
                setErrorMessage('Unable to load request.')
            })
    }, [id])

    function prevMonth() {
        setCalDate(new Date(calDate.getFullYear(), calDate.getMonth() - 1, 1))
        setSelectedDay(null)
    }

    function nextMonth() {
        setCalDate(new Date(calDate.getFullYear(), calDate.getMonth() + 1, 1))
        setSelectedDay(null)
    }

    function isToday(day) {
        return day === today.getDate() &&
            calDate.getMonth() === today.getMonth() &&
            calDate.getFullYear() === today.getFullYear()
    }

    function formatDateForInput(date) {
        const y = date.getFullYear()
        const m = String(date.getMonth() + 1).padStart(2, '0')
        const d = String(date.getDate()).padStart(2, '0')
        return `${y}-${m}-${d}`
    }

    function handleDayClick(day) {
        if (!day || !canEdit) return

        const clickedDate = new Date(calDate.getFullYear(), calDate.getMonth(), day)
        const dateStr = formatDateForInput(clickedDate)

        setFromDate(dateStr)
        setSelectedDay(day)
        setErrorMessage('')
    }

    function handleFromDateChange(event) {
        const dateStr = event.target.value
        setFromDate(dateStr)

        if (!dateStr) {
            setSelectedDay(null)
            return
        }

        const [year, month, day] = dateStr.split('-').map(Number)
        setCalDate(new Date(year, month - 1, 1))
        setSelectedDay(day)
    }

    function handleSave() {
        setErrorMessage('')

        if (!canEdit) {
            setErrorMessage('Only pending requests can be edited.')
            return
        }

        if (!fromDate) {
            setErrorMessage('Please enter a start date.')
            return
        }

        const payload = {
            employeeId,
            startDate: fromDate,
            endDate: toDate || fromDate,
            startTime: fromTime || null,
            endTime: toTime || null,
            reason,
            status: 'PENDING',
            emergency: requestType === 'emergency'
        }

        updateTimeOffRequest(id, payload)
            .then(() => {
                navigate('/employee/requests')
            })
            .catch((error) => {
                console.error('Failed to update request:', error)
                setErrorMessage('Failed to update request. Please try again.')
            })
    }

    function handleDelete() {
        const confirmDelete = window.confirm('Are you sure you want to delete this request?')

        if (!confirmDelete) return

        deleteTimeOffRequest(id)
            .then(() => {
                navigate('/employee/requests')
            })
            .catch((error) => {
                console.error('Failed to delete request:', error)
                setErrorMessage('Failed to delete request. Please try again.')
            })
    }

    return (
        <div className="request-timeoff-page">
            <EmployeeHeader />

            <main className="request-timeoff-content">
                <h1 className="request-timeoff-title">Edit Time Off Request</h1>
                <hr className="request-timeoff-divider" />

                {errorMessage && (
                    <div className="request-error-banner">
                        {errorMessage}
                    </div>
                )}

                {status && status !== 'PENDING' && (
                    <div className="request-error-banner">
                        This request is {status}. It can no longer be edited.
                    </div>
                )}

                <div className="request-timeoff-body">
                    <div className="request-timeoff-left">
                        <div className="request-type-row">
                            <span className="request-type-label">Request Type:</span>

                            <button
                                type="button"
                                disabled={!canEdit}
                                className={`request-type-btn ${requestType === 'timeoff' ? 'active-timeoff' : ''}`}
                                onClick={() => setRequestType('timeoff')}
                            >
                                Time Off
                            </button>

                            <button
                                type="button"
                                disabled={!canEdit}
                                className={`request-type-btn ${requestType === 'emergency' ? 'active-emergency' : ''}`}
                                onClick={() => setRequestType('emergency')}
                            >
                                Emergency
                            </button>
                        </div>

                        <div>
                            <p className="time-section-label">Enter time:</p>

                            <div className="time-row">
                                <label>From:</label>
                                <input
                                    className="time-input"
                                    type="date"
                                    value={fromDate}
                                    disabled={!canEdit}
                                    onChange={handleFromDateChange}
                                />
                                <input
                                    className="time-input"
                                    type="time"
                                    value={fromTime}
                                    disabled={!canEdit}
                                    onChange={(e) => setFromTime(e.target.value)}
                                />
                            </div>

                            <div className="time-row">
                                <label>To:</label>
                                <input
                                    className="time-input"
                                    type="date"
                                    value={toDate}
                                    disabled={!canEdit}
                                    onChange={(e) => setToDate(e.target.value)}
                                />
                                <input
                                    className="time-input"
                                    type="time"
                                    value={toTime}
                                    disabled={!canEdit}
                                    onChange={(e) => setToTime(e.target.value)}
                                />
                            </div>
                        </div>

                        <div>
                            <span className="reason-label">Reason:</span>
                            <textarea
                                className="reason-textarea"
                                value={reason}
                                disabled={!canEdit}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Enter reason..."
                            />
                        </div>
                    </div>

                    <div className="request-timeoff-right">
                        <div className="mini-calendar-card">
                            <div className="mini-calendar-controls">
                                <button type="button" onClick={prevMonth}>‹</button>
                                <span style={{ fontWeight: 'bold', fontSize: '15px' }}>
                                    {monthName} {year}
                                </span>
                                <button type="button" onClick={nextMonth}>›</button>
                            </div>

                            <div className="mini-calendar-grid">
                                {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(dayName => (
                                    <div key={dayName} className="mini-cal-header">
                                        {dayName}
                                    </div>
                                ))}

                                {cells.map((day, index) => (
                                    <div
                                        key={index}
                                        onClick={() => handleDayClick(day)}
                                        className={[
                                            'mini-cal-day',
                                            !day ? 'empty' : '',
                                            isToday(day) ? 'today' : '',
                                            selectedDay === day ? 'selected' : ''
                                        ].join(' ')}
                                    >
                                        {day || ''}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="request-timeoff-actions">
                    <div className="request-primary-actions">
                        {canEdit && (
                            <button
                                className="submit-request-btn"
                                onClick={handleSave}
                            >
                                Save Changes
                            </button>
                        )}

                        <button
                            className="back-request-btn"
                            onClick={() => navigate('/employee/requests')}
                        >
                            Back
                        </button>
                    </div>

                    {canEdit && (
                        <button
                            className="delete-request-btn"
                            onClick={handleDelete}
                        >
                            Delete Request
                        </button>
                    )}
                </div>
            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default EmployeeRequestTimeOffEditPage