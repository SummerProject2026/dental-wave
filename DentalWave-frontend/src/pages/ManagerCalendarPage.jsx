import '../App.css'
import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import ManagerHeader from '../components/ManagerHeader'
import PrintableSchedule from '../components/PrintableSchedule'
import {
    getAllCalendars,
    generateCalendar,
    updateCalendar,
    deleteCalendar,
    addScheduleToCalendar,
    removeScheduleFromCalendar
} from '../services/CalendarService'
import {
    assignEmployeeToTeam,
    assignResourceToTeam,
    createTeam,
    deleteTeam,
    removeEmployeeFromTeam,
    removeResourceFromTeam,
    renameTeam,
    updateEmployeePartialDayNote,
    updateResourcePartialDayNote,
    updateSchedule
} from '../services/ScheduleService'
import { getAllEmployees, getEmployeesByOffice } from '../services/EmployeeService'
import { getAllTimeOffRequests } from '../services/TimeOffRequestService'
import { getLoggedInUserId } from '../services/AuthService'
import { getAllOffices } from '../services/OfficeService'
import { getResources } from '../services/ManagerSchedulerService'
import {
    buildPrintWeeks,
    createDoctorPrintAbbreviationMap,
    getDoctorPrintAbbreviation,
    sortOfficePrintSchedules
} from '../utils/printScheduleUtils'

const OFFICES = [
    { id: 1, name: 'Raleigh' },
    { id: 2, name: 'Garner' },
    { id: 3, name: 'Smithfield' }
]

const TEAM_COLORS = [
    '#a32626',
    '#164f44',
    '#2444d8',
    '#6d3a85',
    '#5d4a00',
    '#0082a6',
    '#b05a00',
    '#2f6d1f',
    '#8b1e55',
    '#4a4f00'
]

function parseLocalDate(dateValue) {
    if (!dateValue) return null

    const [datePart] = String(dateValue).split('T')
    const [year, month, day] = datePart.split('-').map(Number)

    if (!year || !month || !day) return null
    return new Date(year, month - 1, day)
}

function getDateKey(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

function getEmployeeName(employee) {
    return `${employee.firstName || ''} ${employee.lastName || ''}`.trim()
}

function ManagerCalendarPage({ previewMode = false }) {

    const today = new Date()
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const requestedMonth = searchParams.get('month')
    const requestedMonthParts = requestedMonth?.split('-').map(Number)
    const initialDate = requestedMonthParts?.length === 2
        && requestedMonthParts.every(Number.isFinite)
        ? new Date(requestedMonthParts[0], requestedMonthParts[1] - 1, 1)
        : new Date(today.getFullYear(), today.getMonth(), 1)
    const [currentDate, setCurrentDate] = useState(initialDate)
    const [offices, setOffices] = useState(OFFICES)
    const [selectedOfficeId, setSelectedOfficeId] = useState('')
    const [calendars, setCalendars] = useState([])
    const [employees, setEmployees] = useState([])
    const [officeEmployees, setOfficeEmployees] = useState([])
    const [schedulingAssistants, setSchedulingAssistants] = useState([])
    const [approvedTimeOffRequests, setApprovedTimeOffRequests] = useState([])
    const [validationIssues, setValidationIssues] = useState([])
    const [showValidationModal, setShowValidationModal] = useState(false)
    const [selectedDay, setSelectedDay] = useState(null)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState(() => {
        if (searchParams.get('generated') === '1') {
            return 'Draft generated. Select any scheduled day below to review and adjust its teams.'
        }
        if (searchParams.get('existing') === '1') {
            return 'A schedule already exists for this month. You can continue editing it below.'
        }
        return ''
    })
    const [loading, setLoading] = useState(false)
    const [editingTeamId, setEditingTeamId] = useState(null)
    const [editingTeamName, setEditingTeamName] = useState('')
    const [newTeamName, setNewTeamName] = useState('')
    const [editingScheduleNotes, setEditingScheduleNotes] = useState('')
    const [partialDayEditor, setPartialDayEditor] = useState(null)
    const [locationToAdd, setLocationToAdd] = useState('')
    const [assistantPrintSize, setAssistantPrintSize] = useState('medium')

    const monthName = currentDate.toLocaleString('default', { month: 'long' })
    const year = currentDate.getFullYear()
    const monthLabel = `${monthName} ${year}`

    function loadOffices() {
        getAllOffices()
            .then((response) => {
                const loadedOffices = response.data || []
                if (loadedOffices.length === 0) return

                setOffices(loadedOffices)
                setSelectedOfficeId((currentOfficeId) => currentOfficeId || loadedOffices[0].id)
            })
            .catch((err) => {
                console.error('Failed to load offices', err)
                setSelectedOfficeId((currentOfficeId) => currentOfficeId || OFFICES[0].id)
            })
    }

    function loadCalendars() {
        getAllCalendars()
            .then((response) => setCalendars(response.data || []))
            .catch((err) => console.error('Failed to load calendars', err))
    }

    function loadEmployees() {
        getAllEmployees()
            .then((response) => setEmployees(response.data || []))
            .catch((err) => console.error('Failed to load employees', err))
    }

    function loadApprovedTimeOffRequests() {
        getAllTimeOffRequests()
            .then((response) => {
                const approved = (response.data || []).filter(
                    (r) => r.status === 'APPROVED'
                )
                setApprovedTimeOffRequests(approved)
            })
            .catch((err) => console.error('Failed to load time-off requests', err))
    }

    useEffect(() => {
        loadOffices()
        loadCalendars()
        loadEmployees()
        loadApprovedTimeOffRequests()
        getResources('ASSISTANT')
            .then((response) => setSchedulingAssistants(
                (response.data || []).map((assistant) => ({
                    ...assistant,
                    firstName: assistant.displayName,
                    lastName: '',
                    position: 'ASSISTANT',
                    status: assistant.active ? 'ACTIVE' : 'INACTIVE',
                    schedulingResource: true
                }))
            ))
            .catch((err) => console.error('Failed to load scheduling assistants', err))
    }, [])

    useEffect(() => {
        if (!selectedOfficeId) return
        getEmployeesByOffice(selectedOfficeId)
            .then((response) => setOfficeEmployees(response.data || []))
            .catch((err) => console.error('Failed to load office employees', err))
    }, [selectedOfficeId])

    function prevMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
        setSelectedDay(null)
    }

    function nextMonth() {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
        setSelectedDay(null)
    }

    function handleMonthChange(value) {
        if (!value) return
        const [selectedYear, selectedMonth] = value.split('-').map(Number)
        setCurrentDate(new Date(selectedYear, selectedMonth - 1, 1))
        setSelectedDay(null)
        setError('')
        setSuccess('')
    }

    function handlePrint() {
        window.print()
    }

    function openPrintPreview() {
        navigate(`/manager/schedule/preview?month=${year}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`)
    }

    const activeCalendar = calendars.find(
        (cal) => cal.officeId === selectedOfficeId && cal.month === monthLabel
    )

    function getStartAndEndDates() {
        const start = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
        const end = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0)

        const format = (d) => {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            return `${y}-${m}-${day}`
        }

        return { startDate: format(start), endDate: format(end) }
    }

    async function loadCalendarsAndReturn() {
        const response = await getAllCalendars()
        setCalendars(response.data || [])
        return response.data || []
    }

    /**
     * Returns a Set of employee ids who have approved time-off
     * overlapping the given date string (YYYY-MM-DD).
     */
    function getEmployeeIdsOnTimeOff(dateStr) {
        if (!dateStr) return new Set()
        const date = parseLocalDate(dateStr)
        if (!date) return new Set()
        const ids = new Set()

        approvedTimeOffRequests.forEach((request) => {
            if (!request.startDate || !request.endDate) return
            const start = parseLocalDate(request.startDate)
            const end = parseLocalDate(request.endDate)
            if (!start || !end) return
            if (date >= start && date <= end) {
                // request.employeeId links back to the employee
                if (request.employeeId) ids.add(request.employeeId)
            }
        })

        return ids
    }

    async function handleNewCalendar() {
        if (monthCalendars.length > 0 && !window.confirm(
            `Regenerate ${monthLabel}? This will replace the current draft and randomly redistribute assistants.`
        )) {
            return
        }

        setLoading(true)
        setError('')
        setSuccess('')

        try {
            const { startDate, endDate } = getStartAndEndDates()
            const createdById = getLoggedInUserId()
            const publishedCalendars = monthCalendars.filter((calendar) => calendar.published)

            if (publishedCalendars.length > 0) {
                setError('This month is already published. Save it as a draft before generating a new monthly draft.')
                return
            }

            setSelectedDay(null)

            for (const calendar of monthCalendars) {
                await deleteCalendar(calendar.id)
            }

            for (const office of offices) {
                await generateCalendar({
                    month: monthLabel,
                    startCalendarDate: startDate,
                    endCalendarDate: endDate,
                    createdById,
                    officeId: office.id
                })
            }

            await loadCalendarsAndReturn()
            if (!selectedOfficeId && offices[0]) {
                setSelectedOfficeId(offices[0].id)
            }
            setSuccess('Draft regenerated. Assistants were randomly redistributed across each doctor team.')
        } catch (err) {
            console.error('Failed to generate calendar', err)
            if (err.response?.status === 401) {
                setError('Your login session is no longer valid. Please log out and log back in, then create the calendar again.')
            } else {
                setError('Failed to generate the monthly draft. Please try again.')
            }
        } finally {
            setLoading(false)
        }
    }

    function getScheduledAssignmentsForMonth() {
        const assignments = []

        monthCalendars.forEach((calendar) => {
            ;(calendar.schedules || []).forEach((schedule) => {
                Object.entries(schedule.teams || {}).forEach(([teamId, teamEmployees]) => {
                    ;(teamEmployees || []).forEach((employee) => {
                        assignments.push({
                            employee,
                            employeeId: employee.id,
                            date: schedule.date,
                            teamId,
                            officeId: calendar.officeId
                        })
                    })
                })
            })
        })

        return assignments
    }

    function buildPublishValidationIssues() {
        const issues = []
        const assignments = getScheduledAssignmentsForMonth()
        const scheduledEmployeeIds = new Set(assignments.map((assignment) => assignment.employeeId))
        const activeAssistants = employees.filter((employee) =>
            employee.status !== 'INACTIVE' &&
            String(employee.position || '').toLowerCase().includes('assistant')
        )

        const unscheduledAssistants = activeAssistants.filter((employee) => !scheduledEmployeeIds.has(employee.id))
        if (unscheduledAssistants.length > 0) {
            issues.push(`${unscheduledAssistants.length} active assistant${unscheduledAssistants.length === 1 ? ' has' : 's have'} no assigned shifts.`)
        }

        let unassignedTeamCount = 0
        monthCalendars.forEach((calendar) => {
            ;(calendar.schedules || []).forEach((schedule) => {
                Object.values(schedule.teams || {}).forEach((teamEmployees) => {
                    if (!teamEmployees || teamEmployees.length === 0) {
                        unassignedTeamCount += 1
                    }
                })
            })
        })
        if (unassignedTeamCount > 0) {
            issues.push(`${unassignedTeamCount} team${unassignedTeamCount === 1 ? ' has' : 's have'} no assigned assistants.`)
        }

        const sameDayAssignments = new Map()
        assignments.forEach((assignment) => {
            const key = `${assignment.employeeId}-${assignment.date}`
            const current = sameDayAssignments.get(key) || new Set()
            current.add(Number(assignment.officeId))
            sameDayAssignments.set(key, current)
        })

        const duplicateSameDayAssignments = Array.from(sameDayAssignments.values())
            .filter((officeIds) => officeIds.size > 1)
        if (duplicateSameDayAssignments.length > 0) {
            issues.push(`${duplicateSameDayAssignments.length} assistant assignment${duplicateSameDayAssignments.length === 1 ? ' is' : 's are'} scheduled in multiple offices on the same day.`)
        }

        const leaveConflictIds = new Set()
        approvedTimeOffRequests.forEach((request) => {
            assignments.forEach((assignment) => {
                if (assignment.employeeId !== request.employeeId) return
                const assignedDate = parseLocalDate(assignment.date)
                const start = parseLocalDate(request.startDate)
                const end = parseLocalDate(request.endDate)
                if (assignedDate && start && end && assignedDate >= start && assignedDate <= end) {
                    leaveConflictIds.add(`${assignment.employeeId}-${assignment.date}`)
                }
            })
        })
        if (leaveConflictIds.size > 0) {
            issues.push(`${leaveConflictIds.size} approved time-off request${leaveConflictIds.size === 1 ? ' conflicts' : 's conflict'} with scheduled shifts.`)
        }

        const officeMismatchIds = new Set()
        assignments.forEach((assignment) => {
            const allowedOfficeIds = (assignment.employee.offices || []).map((office) => Number(office.id))
            if (allowedOfficeIds.length > 0 && !allowedOfficeIds.includes(Number(assignment.officeId))) {
                officeMismatchIds.add(`${assignment.employeeId}-${assignment.officeId}`)
            }
        })
        if (officeMismatchIds.size > 0) {
            issues.push(`${officeMismatchIds.size} assignment${officeMismatchIds.size === 1 ? ' is' : 's are'} outside the employee's office locations.`)
        }

        return issues
    }

    async function publishMonth() {
        setLoading(true)
        setError('')
        setSuccess('')
        try {
            await Promise.all(monthCalendars
                .filter((calendar) => !calendar.published)
                .map((calendar) => updateCalendar(calendar.id, {
                    ...calendar,
                    published: true
                })))
            await loadCalendarsAndReturn()
            setSuccess('Published the monthly schedule as the Universal Calendar.')
        } catch (err) {
            console.error('Failed to publish calendar', err)
            if (err.response?.status === 401) {
                setError('Your login session is no longer valid. Please log out and log back in, then publish again.')
            } else {
                setError(
                    err.response?.data?.message
                    || err.response?.data?.detail
                    || (typeof err.response?.data === 'string' ? err.response.data : null)
                    || 'Failed to publish calendar.'
                )
            }
        } finally {
            setLoading(false)
            setShowValidationModal(false)
        }
    }

    async function handlePublish() {
        if (monthCalendars.length === 0) {
            setError('Create the monthly calendar first before publishing.')
            return
        }

        const issues = buildPublishValidationIssues()
        if (issues.length > 0) {
            setValidationIssues(issues)
            setShowValidationModal(true)
            return
        }

        await publishMonth()
    }

    async function handleSaveDraft() {
        if (monthCalendars.length === 0) {
            setError('Create the monthly calendar first before saving a draft.')
            return
        }

        setLoading(true)
        setError('')
        setSuccess('')
        try {
            await Promise.all(monthCalendars
                .filter((calendar) => calendar.published)
                .map((calendar) => updateCalendar(calendar.id, {
                    ...calendar,
                    published: false
                })))
            await loadCalendarsAndReturn()
            setSuccess('Draft saved. You can come back and keep editing this month.')
        } catch (err) {
            console.error('Failed to save as draft', err)
            if (err.response?.status === 401) {
                setError('Your login session is no longer valid. Please log out and log back in, then save again.')
            } else {
                setError('Failed to save as draft.')
            }
        } finally {
            setLoading(false)
        }
    }

    function isSunday(day) {
        if (!day) return false
        return new Date(currentDate.getFullYear(), currentDate.getMonth(), day).getDay() === 0
    }

    async function handleRenameTeam(teamId) {
        if (!editingTeamName.trim()) return
        setLoading(true)
        setError('')
        try {
            await renameTeam(teamId, editingTeamName.trim())
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to rename team', err)
            setError('Failed to rename team.')
        } finally {
            setLoading(false)
            setEditingTeamId(null)
            setEditingTeamName('')
        }
    }

    async function handleCreateTeam() {
        if (!selectedSchedule || !newTeamName.trim()) return

        setLoading(true)
        setError('')
        try {
            await createTeam(selectedSchedule.id, newTeamName.trim())
            await loadCalendarsAndReturn()
            setNewTeamName('')
        } catch (err) {
            console.error('Failed to add team', err)
            setError('Failed to add team.')
        } finally {
            setLoading(false)
        }
    }

    async function handleDeleteTeam(teamId) {
        setLoading(true)
        setError('')
        try {
            await deleteTeam(teamId)
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to delete team', err)
            setError('Failed to delete team.')
        } finally {
            setLoading(false)
        }
    }

    async function handleSaveScheduleNotes() {
        if (!selectedSchedule) return

        setLoading(true)
        setError('')
        try {
            await updateSchedule(selectedSchedule.id, {
                ...selectedSchedule,
                notes: editingScheduleNotes
            })
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to save notes', err)
            setError('Failed to save notes.')
        } finally {
            setLoading(false)
        }
    }

    async function handleAddEmployeeToTeam(scheduleId, teamId, selectedValue) {
        if (!selectedValue) return
        const [source, rawId] = String(selectedValue).split(':')
        const employeeId = Number(rawId)

        setLoading(true)
        setError('')
        try {
            if (source === 'resource') {
                await assignResourceToTeam(scheduleId, teamId, employeeId)
            } else {
                await assignEmployeeToTeam(scheduleId, teamId, employeeId)
            }
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to add employee to team', err)
            setError('Failed to add employee to team.')
        } finally {
            setLoading(false)
        }
    }

    async function handleRemoveEmployeeFromTeam(scheduleId, teamId, employee) {
        setLoading(true)
        setError('')
        try {
            if (employee.schedulingResource) {
                await removeResourceFromTeam(scheduleId, teamId, employee.id)
            } else {
                await removeEmployeeFromTeam(scheduleId, teamId, employee.id)
            }
            await loadCalendarsAndReturn()
        } catch (err) {
            console.error('Failed to remove employee from team', err)
            setError('Failed to remove employee from team.')
        } finally {
            setLoading(false)
        }
    }

    function getPartialDayEditorKey(teamId, employee) {
        const source = employee.schedulingResource ? 'resource' : 'employee'
        return `${teamId}:${source}:${employee.id}`
    }

    function openPartialDayEditor(teamId, employee) {
        setPartialDayEditor({
            key: getPartialDayEditorKey(teamId, employee),
            note: employee.partialDayNote || ''
        })
    }

    async function handleSavePartialDayNote(scheduleId, teamId, employee) {
        if (!partialDayEditor) return
        const note = partialDayEditor.note.trim()
        if (note.length > 40) {
            setError('Assistant notes must be 40 characters or fewer.')
            return
        }

        setLoading(true)
        setError('')
        try {
            if (employee.schedulingResource) {
                await updateResourcePartialDayNote(
                    scheduleId, teamId, employee.id, note
                )
            } else {
                await updateEmployeePartialDayNote(
                    scheduleId, teamId, employee.id, note
                )
            }
            await loadCalendarsAndReturn()
            setPartialDayEditor(null)
        } catch (err) {
            console.error('Failed to save partial-day details', err)
            setError(
                err.response?.data?.message
                || err.response?.data?.detail
                || 'Failed to save partial-day details.'
            )
        } finally {
            setLoading(false)
        }
    }

    const firstDayOfWeek = currentDate.getDay()
    const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate()

    const cells = []
    for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    const scheduleByDay = {}
    if (activeCalendar) {
        ;(activeCalendar.schedules || []).forEach((schedule) => {
            if (!schedule.date) return
            const scheduleDate = parseLocalDate(schedule.date)
            if (!scheduleDate) return
            const day = scheduleDate.getDate()
            scheduleByDay[day] = schedule
        })
    }

    const selectedSchedule = selectedDay ? scheduleByDay[selectedDay] : null

    // Build the selected date string for time-off filtering
    const selectedDateStr = selectedDay
        ? `${year}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(selectedDay).padStart(2, '0')}`
        : null

    const employeesOnTimeOff = getEmployeeIdsOnTimeOff(selectedDateStr)
    const monthCalendars = calendars.filter((cal) => cal.month === monthLabel)

    function getCalendarForOffice(officeId) {
        return monthCalendars.find((calendar) => Number(calendar.officeId) === Number(officeId))
    }

    function getScheduleForDay(calendar, day) {
        if (!calendar || !day) return null
        return (calendar.schedules || []).find((schedule) => {
            const date = parseLocalDate(schedule.date)
            return date?.getDate() === Number(day) && date.getMonth() === currentDate.getMonth()
        })
    }

    const selectedDayLocations = selectedDay
        ? offices.map((office) => {
            const calendar = getCalendarForOffice(office.id)
            return { office, calendar, schedule: getScheduleForDay(calendar, selectedDay) }
        }).filter(({ schedule }) => Boolean(schedule))
        : []

    const availableLocationsForSelectedDay = selectedDay
        ? offices.filter((office) => !selectedDayLocations.some(
            ({ office: activeOffice }) => Number(activeOffice.id) === Number(office.id)
        ))
        : []

    async function handleAddLocationToDay() {
        if (!selectedDay || !locationToAdd) return
        const calendar = getCalendarForOffice(Number(locationToAdd))
        if (!calendar) {
            setError('Generate the monthly draft before adding a location to a day.')
            return
        }
        setLoading(true)
        setError('')
        try {
            await addScheduleToCalendar(calendar.id, {
                date: selectedDateStr,
                startScheduleDate: selectedDateStr,
                endScheduleDate: selectedDateStr,
                published: false,
                teams: {},
                teamNames: {}
            })
            await loadCalendarsAndReturn()
            setSelectedOfficeId(Number(locationToAdd))
            setLocationToAdd('')
            setSuccess('Location added to this date only. Add its doctor team and assistants next.')
        } catch (err) {
            console.error('Failed to add location to day', err)
            setError(err.response?.data?.message || 'Failed to add the location to this date.')
        } finally {
            setLoading(false)
        }
    }

    async function handleRemoveLocationFromDay(calendar, schedule, office) {
        const assignmentCount = Object.values(schedule.teams || {})
            .reduce((total, team) => total + (team?.length || 0), 0)
        if (assignmentCount > 0 || Object.keys(schedule.teams || {}).length > 0) {
            setError(`Remove all doctor teams and assignments from ${office.name} before removing that location.`)
            return
        }
        if (!window.confirm(`Remove ${office.name} from ${monthName} ${selectedDay}?`)) return
        setLoading(true)
        setError('')
        try {
            await removeScheduleFromCalendar(calendar.id, schedule.id)
            await loadCalendarsAndReturn()
            if (Number(selectedOfficeId) === Number(office.id)) {
                const fallback = selectedDayLocations.find(
                    ({ office: item }) => Number(item.id) !== Number(office.id)
                )
                if (fallback) setSelectedOfficeId(Number(fallback.office.id))
            }
            setSuccess(`${office.name} removed from this date only.`)
        } catch (err) {
            console.error('Failed to remove location from day', err)
            setError('Failed to remove the location from this date.')
        } finally {
            setLoading(false)
        }
    }

    const isToday = (day) =>
        day === today.getDate() &&
        currentDate.getMonth() === today.getMonth() &&
        currentDate.getFullYear() === today.getFullYear()

    const monthStatus = monthCalendars.length === 0
        ? 'No calendar created'
        : monthCalendars.every((calendar) => calendar.published)
            ? 'Published Universal Calendar'
            : 'Draft'
    const officeNameById = offices.reduce((names, office) => {
        names[office.id] = office.name
        return names
    }, {})

    function getScheduleForDate(calendar, dateKey) {
        return (calendar.schedules || []).find((schedule) => {
            const scheduleDate = parseLocalDate(schedule.date)
            return scheduleDate && getDateKey(scheduleDate) === dateKey
        })
    }

    function getOfficePrintSchedules(date) {
        const dateKey = getDateKey(date)

        const officeSchedules = monthCalendars
            .map((calendar) => ({
                officeName: officeNameById[calendar.officeId] || calendar.officeName || 'Office',
                schedule: getScheduleForDate(calendar, dateKey)
            }))
            .filter(({ schedule }) => Boolean(schedule))

        return sortOfficePrintSchedules(officeSchedules)
    }

    function getTeamName(schedule, teamId) {
        const teamIdNum = Number(teamId)
        return schedule.teamNames?.[teamIdNum] || `Team ${teamId}`
    }

    function getDoctorPrintName(teamName, employees) {
        const doctor = employees.find((employee) =>
            String(employee.position || '').toLowerCase().includes('doctor')
        )
        const sourceName = doctor && /^team\s+\d+$/i.test(teamName)
            ? getEmployeeName(doctor || employees[0] || {})
            : teamName

        if (/no\s*dr/i.test(sourceName)) return 'NO DR'

        const cleaned = sourceName
            .replace(/^[a-zA-Z]+\)\s*/, '')
            .replace(/^dr\.?\s*/i, '')
            .replace(/\bdr\.?\b/gi, '')
            .replace(/[^a-zA-Z\s-]/g, ' ')
            .trim()

        return cleaned || teamName
    }

    const doctorPrintAbbreviations = createDoctorPrintAbbreviationMap(
        monthCalendars.flatMap((calendar) => (calendar.schedules || [])
            .flatMap((schedule) => Object.entries(schedule.teams || {})
                .map(([teamId, teamEmployees]) => getDoctorPrintName(
                    getTeamName(schedule, teamId), teamEmployees
                ))
                .filter((name) => name !== 'NO DR')
            )
        )
    )

    function getDoctorIdentifier(teamName, teamEmployees) {
        const doctorName = getDoctorPrintName(teamName, teamEmployees)
        if (doctorName === 'NO DR') return doctorName
        return getDoctorPrintAbbreviation(doctorName, doctorPrintAbbreviations)
    }

    function getPrintableAssistants(teamName, employees) {
        return employees.filter((employee) =>
            !String(employee.position || '').toLowerCase().includes('doctor')
        )
    }

    function getPrintableFirstName(employee) {
        return employee.firstName || getEmployeeName(employee).split(/\s+/)[0] || ''
    }

    const printWeeks = buildPrintWeeks(year, currentDate.getMonth())
    const printMonthLabel = `${monthName} '${String(year).slice(-2)}`

    const printTeamColorByDoctor = {}
    monthCalendars.forEach((calendar) => {
        ;(calendar.schedules || []).forEach((schedule) => {
            Object.entries(schedule.teams || {}).forEach(([teamId, employees]) => {
                const doctorIdentifier = getDoctorIdentifier(getTeamName(schedule, teamId), employees)
                if (printTeamColorByDoctor[doctorIdentifier] === undefined) {
                    printTeamColorByDoctor[doctorIdentifier] = Object.keys(printTeamColorByDoctor).length
                }
            })
        })
    })

    function getPrintTeamColorClass(doctorIdentifier) {
        return `print-team-color-${printTeamColorByDoctor[doctorIdentifier] % 10}`
    }

    function getTeamColorStyle(doctorIdentifier) {
        const colorIndex = printTeamColorByDoctor[doctorIdentifier] ?? 0
        const color = TEAM_COLORS[colorIndex % TEAM_COLORS.length]
        return {
            color,
            borderLeftColor: color
        }
    }

    const approvedRequestsForMonth = approvedTimeOffRequests.filter((request) => {
        const start = parseLocalDate(request.startDate)
        const end = parseLocalDate(request.endDate)
        if (!start || !end) return false

        const monthStart = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
        const monthEnd = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0)
        return start <= monthEnd && end >= monthStart
    })

    const printableDayNotes = monthCalendars
        .flatMap((calendar) => (calendar.schedules || [])
            .filter((schedule) => schedule.notes?.trim())
            .map((schedule) => {
                const scheduleDate = parseLocalDate(schedule.date)
                return {
                    id: `${calendar.id}-${schedule.id}`,
                    date: scheduleDate,
                    officeName: officeNameById[calendar.officeId]
                        || calendar.officeName
                        || 'Office',
                    note: schedule.notes.trim()
                }
            }))
        .filter((entry) =>
            entry.date && entry.date.getMonth() === currentDate.getMonth()
        )
        .sort((left, right) => left.date - right.date)

    const printableSchedule = (isPreview = false) => (
        <PrintableSchedule
            monthIndex={currentDate.getMonth()}
            weeks={printWeeks}
            monthLabel={printMonthLabel}
            printableDayNotes={printableDayNotes}
            approvedRequests={approvedRequestsForMonth}
            getDateKey={getDateKey}
            getOfficeSchedules={getOfficePrintSchedules}
            getTeamName={getTeamName}
            getDoctorIdentifier={getDoctorIdentifier}
            getPrintableAssistants={getPrintableAssistants}
            getPrintableFirstName={getPrintableFirstName}
            getTeamColorClass={getPrintTeamColorClass}
            assistantNameSize={assistantPrintSize}
            preview={isPreview}
        />
    )

    if (previewMode) {
        return (
            <div className="print-preview-page">
                <div className="print-preview-toolbar">
                    <button type="button" onClick={() => navigate(`/manager/schedule?month=${year}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`)}>
                        &larr; Back to Schedule
                    </button>
                    <fieldset>
                        <legend>Assistant Name Size</legend>
                        {['small', 'medium', 'large'].map((size) => (
                            <button
                                type="button"
                                key={size}
                                className={assistantPrintSize === size ? 'active' : ''}
                                aria-pressed={assistantPrintSize === size}
                                onClick={() => setAssistantPrintSize(size)}
                            >
                                {size[0].toUpperCase() + size.slice(1)}
                            </button>
                        ))}
                    </fieldset>
                    <button type="button" className="print-preview-print-button" onClick={handlePrint} disabled={!monthCalendars.length}>
                        Print
                    </button>
                    <small>Letter landscape · 100% scale · turn browser headers and footers off</small>
                </div>
                <div className="print-preview-sheet">
                    {monthCalendars.length ? printableSchedule(true) : <p>No schedule exists for {monthLabel}.</p>}
                </div>
            </div>
        )
    }

    return (
        <div className="calendar-page">

            <ManagerHeader />

            <main className="manager-schedule-builder">

                <section className="manager-builder-toolbar">
                    <div className="manager-builder-title-block">
                        <h1>Monthly Schedule</h1>
                        <p>{monthStatus}</p>
                    </div>

                    <div className="manager-builder-controls">
                        <label>
                            Month
                            <input
                                type="month"
                                value={`${year}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`}
                                onChange={(e) => handleMonthChange(e.target.value)}
                            />
                        </label>

                        <label>
                            Location
                            <select
                                value={selectedOfficeId}
                                onChange={(e) => {
                                    setSelectedOfficeId(Number(e.target.value))
                                    setSelectedDay(null)
                                }}
                            >
                                {offices.map((office) => (
                                    <option key={office.id} value={office.id}>
                                        {office.name}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <button className="save-draft-btn" onClick={handleNewCalendar} disabled={loading || !offices.length}>
                            {monthCalendars.length ? 'Regenerate & Randomize' : 'Generate Monthly Draft'}
                        </button>
                        <button className="save-draft-btn" onClick={handleSaveDraft} disabled={loading || !monthCalendars.length}>
                            Save Draft
                        </button>
                        <button className="publish-btn" onClick={handlePublish} disabled={loading || !monthCalendars.length}>
                            Publish
                        </button>
                        <button className="print-schedule-btn" onClick={openPrintPreview} disabled={!monthCalendars.length}>
                            Preview Schedule
                        </button>
                    </div>
                </section>

                {error && <p className="error-message manager-builder-error">{error}</p>}
                {success && <p className="success-message manager-builder-error">{success}</p>}

                <section className="manager-builder-workspace">
                    <div className="manager-month-card">
                        <div className="manager-month-nav">
                            <button onClick={prevMonth} disabled={loading}>&lt;</button>
                            <h2>{monthName} {year}</h2>
                            <button onClick={nextMonth} disabled={loading}>&gt;</button>
                        </div>

                        {!activeCalendar && (
                            <div className="manager-calendar-empty-message">
                                No draft exists for this location and month. Use Generate Monthly Draft above.
                            </div>
                        )}

                        <div className="manager-month-grid">
                            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((dayName) => (
                                <div key={dayName} className="manager-month-day-header">{dayName}</div>
                            ))}

                            {cells.map((day, i) => {
                                const schedule = day ? scheduleByDay[day] : null
                                const teams = Object.entries(schedule?.teams || {})
                                const isEditableDay = Boolean(day && monthCalendars.length && !isSunday(day))

                                return (
                                    <button
                                        key={i}
                                        type="button"
                                        className={[
                                            'manager-month-day',
                                            !day ? 'manager-month-day-empty' : '',
                                            isToday(day) ? 'manager-month-day-today' : '',
                                            selectedDay === day ? 'manager-month-day-selected' : '',
                                            isSunday(day) ? 'manager-month-day-closed' : '',
                                            schedule ? 'manager-month-day-scheduled' : ''
                                        ].join(' ')}
                                        onClick={() => {
                                            if (!isEditableDay) return
                                            setSelectedDay(day)
                                            setEditingScheduleNotes(schedule?.notes || '')
                                            setPartialDayEditor(null)
                                        }}
                                        disabled={!isEditableDay}
                                    >
                                        <span className="manager-month-date">{day || ''}</span>

                                        {schedule && (
                                            <div className="manager-day-summary">
                                                {teams.slice(0, 3).map(([teamId, employees]) => {
                                                    const teamName = getTeamName(schedule, teamId)
                                                    const doctorIdentifier = getDoctorIdentifier(teamName, employees)

                                                    return (
                                                        <div
                                                            key={teamId}
                                                            className="manager-day-summary-team"
                                                            style={getTeamColorStyle(doctorIdentifier)}
                                                        >
                                                            <strong>{teamName}</strong>
                                                            <span>{employees.map(getEmployeeName).join(', ') || 'No assistants'}</span>
                                                        </div>
                                                    )
                                                })}
                                                {teams.length > 3 && (
                                                    <span className="manager-day-more">+ {teams.length - 3} more</span>
                                                )}
                                                {schedule.notes && (
                                                    <em>{schedule.notes}</em>
                                                )}
                                            </div>
                                        )}
                                    </button>
                                )
                            })}
                        </div>
                    </div>

                    <aside className="manager-day-editor-panel">
                        {selectedDay && (
                            <section className="manager-day-locations" aria-labelledby="day-locations-heading">
                                <h3 id="day-locations-heading">Locations for {monthName} {selectedDay}</h3>
                                <div className="manager-active-location-list">
                                    {selectedDayLocations.length ? selectedDayLocations.map(({ office, calendar, schedule }) => (
                                        <div key={office.id}>
                                            <button
                                                type="button"
                                                className={Number(selectedOfficeId) === Number(office.id) ? 'active' : ''}
                                                onClick={() => setSelectedOfficeId(Number(office.id))}
                                            >
                                                {office.name}
                                            </button>
                                            <button
                                                type="button"
                                                className="manager-remove-day-location"
                                                onClick={() => handleRemoveLocationFromDay(calendar, schedule, office)}
                                                disabled={loading}
                                                aria-label={`Remove ${office.name} from this date`}
                                            >
                                                Remove
                                            </button>
                                        </div>
                                    )) : <p>No locations are active for this date.</p>}
                                </div>
                                {availableLocationsForSelectedDay.length > 0 && (
                                    <div className="manager-add-day-location">
                                        <select value={locationToAdd} onChange={(event) => setLocationToAdd(event.target.value)}>
                                            <option value="">Select location…</option>
                                            {availableLocationsForSelectedDay.map((office) => (
                                                <option key={office.id} value={office.id}>{office.name}</option>
                                            ))}
                                        </select>
                                        <button type="button" onClick={handleAddLocationToDay} disabled={loading || !locationToAdd}>
                                            + Add Location
                                        </button>
                                    </div>
                                )}
                            </section>
                        )}
                        {selectedSchedule ? (
                            <>
                                <div className="manager-day-editor-header">
                                    <div>
                                        <h3>{monthName} {selectedDay}</h3>
                                        <p>{officeNameById[selectedOfficeId] || 'Selected office'}</p>
                                    </div>
                                    <button onClick={() => {
                                        setSelectedDay(null)
                                        setPartialDayEditor(null)
                                    }}>Close</button>
                                </div>

                                <div className="manager-add-team-row">
                                    <input
                                        value={newTeamName}
                                        onChange={(e) => setNewTeamName(e.target.value)}
                                        onKeyDown={(e) => {
                                            if (e.key === 'Enter') handleCreateTeam()
                                        }}
                                        placeholder="Team label, e.g. L), NO DR"
                                    />
                                    <button onClick={handleCreateTeam} disabled={loading || !newTeamName.trim()}>
                                        Add Team
                                    </button>
                                </div>

                                {Object.entries(selectedSchedule.teams || {}).length > 0 ? (
                                    Object.entries(selectedSchedule.teams || {}).map(([teamId, employees]) => {
                                        const teamIdNum = Number(teamId)
                                        const teamDisplayName = getTeamName(selectedSchedule, teamId)
                                        const assignedIds = new Set(Object.values(selectedSchedule.teams || {})
                                            .flat()
                                            .map((employee) => `${employee.schedulingResource ? 'resource' : 'employee'}:${employee.id}`))
                                        const availableToAdd = [...officeEmployees, ...schedulingAssistants].filter(
                                            (employee) => !assignedIds.has(
                                                `${employee.schedulingResource ? 'resource' : 'employee'}:${employee.id}`
                                            )
                                                && employee.status !== 'INACTIVE'
                                                && !employeesOnTimeOff.has(employee.id)
                                        )

                                        return (
                                            <div key={teamId} className="manager-editor-team-card">
                                                {editingTeamId === teamIdNum ? (
                                                    <div className="team-rename-row">
                                                        <input
                                                            className="team-rename-input"
                                                            value={editingTeamName}
                                                            onChange={(e) => setEditingTeamName(e.target.value)}
                                                            onKeyDown={(e) => {
                                                                if (e.key === 'Enter') handleRenameTeam(teamIdNum)
                                                                if (e.key === 'Escape') { setEditingTeamId(null); setEditingTeamName('') }
                                                            }}
                                                            autoFocus
                                                        />
                                                        <button className="team-rename-save-btn" onClick={() => handleRenameTeam(teamIdNum)} disabled={loading}>Save</button>
                                                        <button className="team-rename-cancel-btn" onClick={() => { setEditingTeamId(null); setEditingTeamName('') }}>Cancel</button>
                                                    </div>
                                                ) : (
                                                    <div className="manager-editor-team-title">
                                                        <strong>{teamDisplayName}</strong>
                                                        <div>
                                                            <button onClick={() => { setEditingTeamId(teamIdNum); setEditingTeamName(teamDisplayName) }}>Edit</button>
                                                            <button onClick={() => handleDeleteTeam(teamIdNum)} disabled={loading}>Delete</button>
                                                        </div>
                                                    </div>
                                                )}

                                                <ul className="manager-editor-assistant-list">
                                                    {employees.map((employee) => {
                                                        const employeeKey = getPartialDayEditorKey(
                                                            teamId, employee
                                                        )
                                                        const isAssistant = !String(
                                                            employee.position || ''
                                                        ).toLowerCase().includes('doctor')
                                                        const isEditingPartialDay =
                                                            partialDayEditor?.key === employeeKey

                                                        return (
                                                            <li key={employeeKey}>
                                                                <div className="manager-assistant-row">
                                                                    <span>
                                                                        {getEmployeeName(employee)}
                                                                        <small>{employee.position}</small>
                                                                        {employee.partialDayNote && (
                                                                            <em className="partial-day-badge">
                                                                                {employee.partialDayNote}
                                                                            </em>
                                                                        )}
                                                                    </span>
                                                                    <div className="manager-assistant-actions">
                                                                        {isAssistant && (
                                                                            <button
                                                                                type="button"
                                                                                className="partial-day-button"
                                                                                onClick={() =>
                                                                                    openPartialDayEditor(
                                                                                        teamId,
                                                                                        employee
                                                                                    )
                                                                                }
                                                                                disabled={loading}
                                                                            >
                                                                                {employee.partialDayNote
                                                                                    ? 'Edit note'
                                                                                    : 'Add note'}
                                                                            </button>
                                                                        )}
                                                                        <button
                                                                            type="button"
                                                                            onClick={() =>
                                                                                handleRemoveEmployeeFromTeam(
                                                                                    selectedSchedule.id,
                                                                                    teamId,
                                                                                    employee
                                                                                )
                                                                            }
                                                                            disabled={loading}
                                                                        >
                                                                            Remove
                                                                        </button>
                                                                    </div>
                                                                </div>

                                                                {isEditingPartialDay && (
                                                                    <div className="partial-day-editor">
                                                                        <label>
                                                                            Assistant note
                                                                            <input
                                                                                value={partialDayEditor.note}
                                                                                maxLength={40}
                                                                                placeholder="Example: out@2"
                                                                                autoFocus
                                                                                onChange={(event) =>
                                                                                    setPartialDayEditor({
                                                                                        ...partialDayEditor,
                                                                                        note: event.target.value
                                                                                    })
                                                                                }
                                                                                onKeyDown={(event) => {
                                                                                    if (event.key === 'Enter') {
                                                                                        handleSavePartialDayNote(
                                                                                            selectedSchedule.id,
                                                                                            teamId,
                                                                                            employee
                                                                                        )
                                                                                    }
                                                                                    if (event.key === 'Escape') {
                                                                                        setPartialDayEditor(null)
                                                                                    }
                                                                                }}
                                                                            />
                                                                        </label>
                                                                        <div>
                                                                            <button
                                                                                type="button"
                                                                                onClick={() =>
                                                                                    handleSavePartialDayNote(
                                                                                        selectedSchedule.id,
                                                                                        teamId,
                                                                                        employee
                                                                                    )
                                                                                }
                                                                                disabled={loading}
                                                                            >
                                                                                Save
                                                                            </button>
                                                                            <button
                                                                                type="button"
                                                                                className="partial-day-cancel"
                                                                                onClick={() =>
                                                                                    setPartialDayEditor(null)
                                                                                }
                                                                                disabled={loading}
                                                                            >
                                                                                Cancel
                                                                            </button>
                                                                        </div>
                                                                    </div>
                                                                )}
                                                            </li>
                                                        )
                                                    })}
                                                </ul>

                                                <select
                                                    className="team-add-select"
                                                    value=""
                                                    onChange={(e) => handleAddEmployeeToTeam(selectedSchedule.id, teamId, e.target.value)}
                                                    disabled={loading}
                                                >
                                                    <option value="">+ Add assistant...</option>
                                                    {availableToAdd.map((employee) => (
                                                        <option
                                                            key={`${employee.schedulingResource ? 'resource' : 'employee'}-${employee.id}`}
                                                            value={`${employee.schedulingResource ? 'resource' : 'employee'}:${employee.id}`}
                                                        >
                                                            {getEmployeeName(employee)} ({employee.position})
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                        )
                                    })
                                ) : (
                                    <p className="manager-empty-editor-note">Add the first team for this day.</p>
                                )}

                                <label className="manager-notes-editor">
                                    Notes &amp; Announcements
                                    <textarea
                                        value={editingScheduleNotes}
                                        onChange={(e) => setEditingScheduleNotes(e.target.value)}
                                        placeholder="Office meeting Thursday at 4:30, closure, reminder..."
                                    />
                                </label>
                                <button className="save-draft-btn" onClick={handleSaveScheduleNotes} disabled={loading}>
                                    Save Notes &amp; Announcements
                                </button>
                            </>
                        ) : (
                            <div className="manager-empty-editor-note">
                                {selectedDay
                                    ? `${officeNameById[selectedOfficeId] || 'This location'} is not active on this date. Add it above or select another active location.`
                                    : 'Select a day to edit its locations and assignments.'}
                            </div>
                        )}
                    </aside>
                </section>

                {printableSchedule(false)}

            </main>

            {showValidationModal && (
                <div className="modal-overlay">
                    <div className="modal-box schedule-validation-modal">
                        <h2>Schedule Validation Warning</h2>
                        <p>The following issues were found:</p>
                        <ul className="validation-modal-list">
                            {validationIssues.map((issue) => (
                                <li key={issue}>{issue}</li>
                            ))}
                        </ul>
                        <p>Would you like to review the schedule or publish anyway?</p>
                        <div className="modal-actions">
                            <button onClick={() => setShowValidationModal(false)}>
                                Review Schedule
                            </button>
                            <button onClick={publishMonth} disabled={loading}>
                                Publish Anyway
                            </button>
                            <button onClick={() => setShowValidationModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

        </div>
    )
}

export default ManagerCalendarPage
