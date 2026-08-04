import './PrintableSchedule.css'
import {
    getAssistantPrintNameClass,
    getOfficePrintTeamCountClass
} from '../utils/printScheduleUtils'

const WORKDAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday']

function getAssignmentCount(officeSchedules) {
    return officeSchedules.reduce((total, { schedule }) => (
        total + Object.values(schedule.teams || {}).reduce(
            (teamTotal, employees) => teamTotal + employees.length,
            0
        )
    ), 0)
}

function getDensityClass(officeSchedules) {
    const assignmentCount = getAssignmentCount(officeSchedules)
    if (assignmentCount >= 18) return 'print-day-density-high'
    if (assignmentCount >= 12) return 'print-day-density-medium'
    return ''
}

export default function PrintableSchedule({
    monthIndex,
    weeks,
    monthLabel,
    monthLabelDateKey,
    notesStartDateKey,
    notesSpan,
    skippedNotesDateKeys,
    printableDayNotes,
    approvedRequests,
    getDateKey,
    getOfficeSchedules,
    getTeamName,
    getDoctorIdentifier,
    getPrintableAssistants,
    getPrintableFirstName,
    getTeamColorClass
}) {
    const weekCount = Math.min(Math.max(weeks.length, 4), 6)
    const hasSupplementalNotes = printableDayNotes.length > 0 || approvedRequests.length > 0
    const safeNotesSpan = Math.min(Math.max(notesSpan, 1), 4)
    const useHeaderFallback = !monthLabelDateKey

    return (
        <section
            className={`manager-calendar-print print-week-count-${weekCount} ${useHeaderFallback && hasSupplementalNotes ? 'print-header-notes-fallback' : ''}`}
            aria-hidden="true"
        >
            <div className="print-calendar-grid">
                {WORKDAYS.map((dayName, index) => (
                    <div
                        key={dayName}
                        className={`print-day-header ${index === 0 ? 'print-day-header-with-month' : ''}`}
                    >
                        {index === 0 && useHeaderFallback && (
                            <span className="print-month-label">{monthLabel}</span>
                        )}
                        <span>{dayName}</span>
                        {index === 0 && useHeaderFallback && hasSupplementalNotes && (
                            <span className="print-header-note-summary">
                                {printableDayNotes.map((entry) => (
                                    <span key={entry.id}>
                                        {entry.date.getMonth() + 1}/{entry.date.getDate()} {entry.officeName}: {entry.note}
                                    </span>
                                ))}
                                {approvedRequests.map((request) => (
                                    <span key={`time-off-${request.id}`}>
                                        Time off: {request.employeeName || request.employeeFullName || `Employee ${request.employeeId}`}{' '}
                                        {request.startDate} - {request.endDate}
                                    </span>
                                ))}
                            </span>
                        )}
                    </div>
                ))}

                {weeks.flatMap((week) => week.map((date) => {
                    const inMonth = date.getMonth() === monthIndex
                    const dateKey = getDateKey(date)
                    const showMonthLabel = !inMonth && dateKey === monthLabelDateKey
                    const showNotes = hasSupplementalNotes && !inMonth && (
                        dateKey === notesStartDateKey || (!notesStartDateKey && showMonthLabel)
                    )
                    const officeSchedules = inMonth ? getOfficeSchedules(date) : []

                    if (hasSupplementalNotes && skippedNotesDateKeys.has(dateKey)) return null

                    const densityClass = getDensityClass(officeSchedules)
                    const cellClasses = [
                        'print-day-cell',
                        !inMonth ? 'print-day-empty' : '',
                        showNotes ? `print-day-notes-cell print-notes-span-${safeNotesSpan}` : '',
                        densityClass
                    ].filter(Boolean).join(' ')

                    return (
                        <div key={dateKey} className={cellClasses}>
                            {showMonthLabel && (
                                <div className="print-unused-month-label">{monthLabel}</div>
                            )}

                            {showNotes && (
                                <div className="print-empty-notes-box">
                                    {printableDayNotes.length > 0 && (
                                        <>
                                            <strong className="print-day-notes-heading">Notes</strong>
                                            <div className="print-day-notes-list">
                                                {printableDayNotes.map((entry) => (
                                                    <span key={entry.id}>
                                                        <b>{entry.date.getMonth() + 1}/{entry.date.getDate()} {entry.officeName}:</b>{' '}
                                                        {entry.note}
                                                    </span>
                                                ))}
                                            </div>
                                        </>
                                    )}

                                    {approvedRequests.length > 0 && (
                                        <div className="print-time-off-section">
                                            <strong className="print-time-off-heading">Approved Time Off</strong>
                                            <div className="print-time-off-list">
                                                {approvedRequests.map((request) => (
                                                    <span key={request.id}>
                                                        {request.employeeName || request.employeeFullName || `Employee ${request.employeeId}`}{' '}
                                                        {request.startDate} - {request.endDate}
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}

                            {inMonth && (
                                <>
                                    <span className="print-day-number">{date.getDate()}</span>
                                    <div className="print-office-stack">
                                        {officeSchedules.map(({ officeName, schedule }) => (
                                            <section
                                                key={`${officeName}-${schedule.id}`}
                                                className={`print-office-block ${getOfficePrintTeamCountClass(schedule)}`}
                                            >
                                                <h2 className="print-office-name">{officeName}</h2>
                                                <div className="print-team-list">
                                                    {Object.entries(schedule.teams || {}).map(([teamId, employees]) => {
                                                        const teamName = getTeamName(schedule, teamId)
                                                        const assistants = getPrintableAssistants(teamName, employees)
                                                        const doctorIdentifier = getDoctorIdentifier(teamName, employees)
                                                        const colorClass = getTeamColorClass(doctorIdentifier)

                                                        return (
                                                            <section key={teamId} className="print-team-group">
                                                                <h3 className={`print-team-name ${colorClass}`}>
                                                                    {doctorIdentifier}
                                                                </h3>
                                                                <div className={`print-assistant-list ${colorClass}`}>
                                                                    {assistants.map((employee) => {
                                                                        const printableName = getPrintableFirstName(employee)

                                                                        return (
                                                                            <div
                                                                                key={`${employee.schedulingResource ? 'resource' : 'employee'}-${employee.id}`}
                                                                                className={employee.partialDayNote ? 'print-assistant-partial' : undefined}
                                                                            >
                                                                                <span className={getAssistantPrintNameClass(printableName)}>
                                                                                    {printableName}
                                                                                </span>
                                                                                {employee.partialDayNote && (
                                                                                    <>
                                                                                        <wbr />
                                                                                        <strong>{` (${employee.partialDayNote})`}</strong>
                                                                                    </>
                                                                                )}
                                                                            </div>
                                                                        )
                                                                    })}
                                                                </div>
                                                            </section>
                                                        )
                                                    })}
                                                </div>
                                            </section>
                                        ))}
                                    </div>
                                </>
                            )}
                        </div>
                    )
                }))}
            </div>
        </section>
    )
}
