import '../App.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import EmployeeHeader from '../components/EmployeeHeader'
import {
    getTimeOffRequestsByEmployee,
    createTimeOffRequest
} from '../services/TimeOffRequestService'
import {
    dateRangeContainsSunday,
    formatDateForInput,
    isPastDateString
} from '../utils/timeOffDateUtils'

function EmployeeRequestTimeOffPage() {
    const navigate = useNavigate()
    const today = new Date()

    // TODO: replace with real logged-in employee id (from auth context/storage)
    const employeeId = Number(sessionStorage.getItem('employeeId'))

    const [requestType, setRequestType] = useState('timeoff')
    const [fromDate, setFromDate] = useState('')
    const [fromTime, setFromTime] = useState('')
    const [toDate, setToDate] = useState('')
    const [toTime, setToTime] = useState('')
    const [reason, setReason] = useState('')
    const [calDate, setCalDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedDay, setSelectedDay] = useState(null)

    const [myRequests, setMyRequests] = useState([])

    const [errorMessage, setErrorMessage] = useState('')

    const monthName = calDate.toLocaleString('default', { month: 'long' })
    const year = calDate.getFullYear()
    const firstDay = calDate.getDay()
    const daysInMonth = new Date(calDate.getFullYear(), calDate.getMonth() + 1, 0).getDate()

    const cells = []
    for (let i = 0; i < firstDay; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    // -------------------------------------------------------------------
    // Load existing requests
    // -------------------------------------------------------------------
    useEffect(() => {
        if (employeeId) {
            getTimeOffRequestsByEmployee(employeeId)
                .then(res => setMyRequests(res.data || []))
                .catch(err => console.error('Failed to load own requests', err))
        }

    }, [employeeId])

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

    // -------------------------------------------------------------------
    // Date helpers
    // -------------------------------------------------------------------

    // Is dateStr within [request.startDate, request.endDate] (inclusive)?
    function dateInRequestRange(dateStr, request) {
        const start = request.startDate
        const end = request.endDate || request.startDate
        return dateStr >= start && dateStr <= end
    }

    // Active = not denied (PENDING or APPROVED both block the date)
    function isActive(request) {
        return request.status === 'PENDING' || request.status === 'APPROVED'
    }

    // Does THIS employee already have a request covering this date?
    function findOwnRequestForDate(dateStr) {
        return myRequests.find(req => isActive(req) && dateInRequestRange(dateStr, req))
    }

    // -------------------------------------------------------------------
    // Calendar day click handler
    // -------------------------------------------------------------------
    function isSunday(day) {
        if (!day) return false
        return new Date(calDate.getFullYear(), calDate.getMonth(), day).getDay() === 0
    }

    function isPastDate(day) {
        if (!day) return false
        const d = new Date(calDate.getFullYear(), calDate.getMonth(), day)
        d.setHours(0, 0, 0, 0)
        const t = new Date()
        t.setHours(0, 0, 0, 0)
        return d < t
    }

    function handleDayClick(day) {
        if (!day) return
        if (isSunday(day)) {
            setErrorMessage('Sundays cannot be selected.')
            return
        }
        if (isPastDate(day)) {
            setErrorMessage('Cannot request time off for a past date.')
            return
        }

        const clickedDate = new Date(calDate.getFullYear(), calDate.getMonth(), day)
        const dateStr = formatDateForInput(clickedDate)

        setErrorMessage('')

        // 1. Already requested by this employee?
        const ownConflict = findOwnRequestForDate(dateStr)
        if (ownConflict) {
            setErrorMessage('Request has already been made for this date.')
            return
        }

        selectDate(dateStr)
    }

    function selectDate(dateStr) {
        setFromDate(dateStr)
        const [y, m, d] = dateStr.split('-').map(Number)
        if (y === calDate.getFullYear() && (m - 1) === calDate.getMonth()) {
            setSelectedDay(d)
        }
    }

    // -------------------------------------------------------------------
    // Manual date input
    // -------------------------------------------------------------------
    function handleFromDateChange(e) {
        const dateStr = e.target.value
        setFromDate(dateStr)
        setErrorMessage('')

        if (!dateStr) {
            setSelectedDay(null)
            return
        }

        const ownConflict = findOwnRequestForDate(dateStr)
        if (ownConflict) {
            setErrorMessage('Request has already been made for this date.')
        }

        const [y, m, d] = dateStr.split('-').map(Number)
        if (y === calDate.getFullYear() && (m - 1) === calDate.getMonth()) {
            setSelectedDay(d)
        } else {
            setSelectedDay(null)
        }
    }

    // -------------------------------------------------------------------
    // Submit
    // -------------------------------------------------------------------
    async function handleSubmit() {
        setErrorMessage('')

        if (!fromDate) {
            setErrorMessage('Please select or enter a date.')
            return
        }

        if (isPastDateString(fromDate)) {
            setErrorMessage('Cannot submit a request for a past date.')
            return
        }

        if (toDate) {
            if (toDate < fromDate) {
                setErrorMessage('End date cannot be before start date.')
                return
            }
            if (fromDate === toDate && fromTime && toTime && fromTime >= toTime) {
                setErrorMessage('End time must be after start time.')
                return
            }
        }

        if (dateRangeContainsSunday(fromDate, toDate || fromDate)) {
            setErrorMessage('Sundays cannot be requested.')
            return
        }

        // Final duplicate check before submitting
        const ownConflict = findOwnRequestForDate(fromDate)
        if (ownConflict) {
            setErrorMessage('Request has already been made for this date.')
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


        try {

            const res = await createTimeOffRequest(payload)
            setMyRequests(prev => [...prev, res.data])
            navigate('/employee/requests')
        } catch (err) {
            console.error('Failed to submit time off request', err)
            if (err.response?.status === 409) {
                setErrorMessage(err.response.data || 'Request has already been made for this date.')
            } else {
                setErrorMessage('Failed to submit request. Please try again.')
            }
        }
    }

    return (
        <div className="request-timeoff-page">
            <EmployeeHeader />

            <main className="request-timeoff-content">
                <h1 className="request-timeoff-title">Request Time Off</h1>
                <hr className="request-timeoff-divider" />

                {errorMessage && (
                    <div className="request-error-banner">
                        {errorMessage}
                    </div>
                )}

                <div className="request-timeoff-body">

                    <div className="request-timeoff-left">

                        <div className="request-type-row">
                            <span className="request-type-label">Request Type:</span>
                            <button
                                className={`request-type-btn ${requestType === 'timeoff' ? 'active-timeoff' : ''}`}
                                onClick={() => setRequestType('timeoff')}
                            >
                                Time Off
                            </button>
                            <button
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
                                    min={formatDateForInput(today)}
                                    onChange={handleFromDateChange}
                                />
                                <input
                                    className="time-input"
                                    type="time"
                                    value={fromTime}
                                    onChange={(e) => setFromTime(e.target.value)}
                                />
                            </div>
                            <div className="time-row">
                                <label>To:</label>
                                <input
                                    className="time-input"
                                    type="date"
                                    value={toDate}
                                    onChange={(e) => setToDate(e.target.value)}
                                />
                                <input
                                    className="time-input"
                                    type="time"
                                    value={toTime}
                                    onChange={(e) => setToTime(e.target.value)}
                                />
                            </div>
                        </div>

                        <div>
                            <span className="reason-label">Reason:</span>
                            <textarea
                                className="reason-textarea"
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Enter reason..."
                            />
                        </div>

                    </div>

                    <div className="request-timeoff-right">
                        <div className="mini-calendar-card">

                            <div className="mini-calendar-controls">
                                <button onClick={prevMonth}>‹</button>
                                <span style={{ fontWeight: 'bold', fontSize: '15px' }}>
                                    {monthName} {year}
                                </span>
                                <button onClick={nextMonth}>›</button>
                            </div>

                            <div className="mini-calendar-grid">
                                {['Su','Mo','Tu','We','Th','Fr','Sa'].map(d => (
                                    <div key={d} className="mini-cal-header">{d}</div>
                                ))}
                                {cells.map((day, i) => (
                                    <div
                                        key={i}
                                        onClick={() => handleDayClick(day)}
                                        className={[
                                            'mini-cal-day',
                                            !day ? 'empty' : '',
                                            isToday(day) ? 'today' : '',
                                            selectedDay === day ? 'selected' : '',
                                            isSunday(day) || isPastDate(day) ? 'cal-day-disabled' : ''
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
                    <button className="submit-request-btn" onClick={handleSubmit}>
                        Submit Request
                    </button>
                    <button className="cancel-request-btn" onClick={() => navigate('/employee/requests')}>
                        Cancel
                    </button>
                </div>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>
        </div>
    )
}

export default EmployeeRequestTimeOffPage
