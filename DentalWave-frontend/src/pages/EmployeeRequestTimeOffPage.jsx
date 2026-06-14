import '../App.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import EmployeeHeader from '../components/EmployeeHeader'

function EmployeeRequestTimeOffPage() {
    const navigate = useNavigate()
    const today = new Date()

    const [requestType, setRequestType] = useState('timeoff')
    const [fromDate, setFromDate] = useState('')
    const [fromTime, setFromTime] = useState('')
    const [toDate, setToDate] = useState('')
    const [toTime, setToTime] = useState('')
    const [reason, setReason] = useState('')
    const [calDate, setCalDate] = useState(new Date(today.getFullYear(), today.getMonth(), 1))
    const [selectedDay, setSelectedDay] = useState(null)

    const monthName = calDate.toLocaleString('default', { month: 'long' })
    const year = calDate.getFullYear()
    const firstDay = calDate.getDay()
    const daysInMonth = new Date(calDate.getFullYear(), calDate.getMonth() + 1, 0).getDate()

    const cells = []
    for (let i = 0; i < firstDay; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

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

    function handleSubmit() {
        // TODO: call API
        navigate('/employee/requests')
    }

    return (
        <div className="request-timeoff-page">
            <EmployeeHeader />

            <main className="request-timeoff-content">
                <h1 className="request-timeoff-title">Request Time Off</h1>
                <hr className="request-timeoff-divider" />

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
                                    onChange={(e) => setFromDate(e.target.value)}
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
                                        onClick={() => day && setSelectedDay(day)}
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