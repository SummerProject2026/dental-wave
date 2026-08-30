import './PrintableSchedule.css'
import {
    getAssistantPrintNameClass,
    getOfficePrintTeamCountClass,
    getPrintWeekCount
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
    printableDayNotes,
    approvedRequests,
    getDateKey,
    getOfficeSchedules,
    getTeamName,
    getDoctorIdentifier,
    getPrintableAssistants,
    getPrintableFirstName,
    getTeamColorClass,
    assistantNameSize = 'medium',
    preview = false
}) {
    const weekCount = getPrintWeekCount(weeks)
    const hasSupplementalNotes = printableDayNotes.length > 0 || approvedRequests.length > 0
    const printDates = weeks.flat()
    const leadingEmptyDates = []
    const trailingEmptyDates = []

    for (const date of printDates) {
        if (date.getMonth() === monthIndex) break
        leadingEmptyDates.push(date)
    }

    for (let index = printDates.length - 1; index >= 0; index -= 1) {
        if (printDates[index].getMonth() === monthIndex) break
        trailingEmptyDates.unshift(printDates[index])
    }

    const availableNoteDates = leadingEmptyDates.length >= trailingEmptyDates.length
        ? leadingEmptyDates
        : trailingEmptyDates
    const notesStartDateKey = hasSupplementalNotes && availableNoteDates.length
        ? getDateKey(availableNoteDates[0])
        : null
    const skippedNoteDateKeys = new Set(
        notesStartDateKey ? availableNoteDates.slice(1).map(getDateKey) : []
    )
    const notesSpan = Math.min(Math.max(availableNoteDates.length, 1), 4)
    const notesEndAtWeekBoundary = availableNoteDates.at(-1)?.getDay() === 4

    const notesContent = (
        <>
            {printableDayNotes.length > 0 && (
                <section>
                    <strong>Notes &amp; Announcements</strong>
                    <div className="print-day-notes-list">
                        {printableDayNotes.map((entry) => (
                            <span key={entry.id}>
                                <b>{entry.date.getMonth() + 1}/{entry.date.getDate()} {entry.officeName}:</b>{' '}
                                {entry.note}
                            </span>
                        ))}
                    </div>
                </section>
            )}
            {approvedRequests.length > 0 && (
                <section className="print-time-off-section">
                    <strong>Approved Time Off</strong>
                    <div className="print-time-off-list">
                        {approvedRequests.map((request) => (
                            <span key={request.id}>
                                {request.employeeName || request.employeeFullName || `Employee ${request.employeeId}`}{' '}
                                {request.startDate} - {request.endDate}
                            </span>
                        ))}
                    </div>
                </section>
            )}
        </>
    )

    return (
        <section
            className={`manager-calendar-print print-assistant-size-${assistantNameSize} print-week-count-${weekCount} ${preview ? 'manager-calendar-print-preview' : ''}`}
            aria-hidden={!preview}
        >
            <header className="print-page-header">
                <h1>{monthLabel}</h1>
            </header>
            <div className="print-calendar-grid">
                {WORKDAYS.map((dayName, index) => (
                    <div
                        key={dayName}
                        className={`print-day-header ${index === 0 ? 'print-day-header-with-month' : ''}`}
                    >
                        <span>{dayName}</span>
                    </div>
                ))}

                {weeks.flatMap((week) => week.map((date, dayIndex) => {
                    const inMonth = date.getMonth() === monthIndex
                    const dateKey = getDateKey(date)
                    const officeSchedules = inMonth ? getOfficeSchedules(date) : []

                    if (skippedNoteDateKeys.has(dateKey)) return null
                    if (dateKey === notesStartDateKey) {
                        return (
                            <aside
                                key={dateKey}
                                className={`print-day-cell print-day-empty print-page-notes print-notes-span-${notesSpan} ${notesEndAtWeekBoundary ? 'print-day-column-last' : ''}`}
                                aria-label="Notes and announcements"
                            >
                                {notesContent}
                            </aside>
                        )
                    }

                    const densityClass = getDensityClass(officeSchedules)
                    const cellClasses = [
                        'print-day-cell',
                        dayIndex === WORKDAYS.length - 1 ? 'print-day-column-last' : '',
                        !inMonth ? 'print-day-empty' : '',
                        densityClass
                    ].filter(Boolean).join(' ')

                    return (
                        <div key={dateKey} className={cellClasses}>
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
                                                                                    <small className="print-assistant-note">
                                                                                        {employee.partialDayNote}
                                                                                    </small>
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
            {hasSupplementalNotes && !notesStartDateKey && (
                <aside className="print-page-notes" aria-label="Notes and announcements">
                    {notesContent}
                </aside>
            )}
        </section>
    )
}
