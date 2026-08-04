export const PRINT_WORKDAY_OFFSETS = [0, 1, 2, 3]
const PRINT_OFFICE_ORDER = ['raleigh', 'garner', 'smithfield']

export function getAssistantPrintNameClass(name) {
    const length = String(name || '').trim().length
    if (length >= 12) return 'print-assistant-name print-assistant-name-very-long'
    if (length >= 9) return 'print-assistant-name print-assistant-name-long'
    return 'print-assistant-name'
}

export function getOfficePrintTeamCountClass(schedule) {
    const teamCount = Math.min(Math.max(Object.keys(schedule?.teams || {}).length, 1), 4)
    return `print-office-team-count-${teamCount}`
}

export function sortOfficePrintSchedules(officeSchedules) {
    return [...officeSchedules].sort((left, right) => {
        const leftName = String(left.officeName || '').trim().toLowerCase()
        const rightName = String(right.officeName || '').trim().toLowerCase()
        const leftIndex = PRINT_OFFICE_ORDER.indexOf(leftName)
        const rightIndex = PRINT_OFFICE_ORDER.indexOf(rightName)
        const leftRank = leftIndex === -1 ? PRINT_OFFICE_ORDER.length : leftIndex
        const rightRank = rightIndex === -1 ? PRINT_OFFICE_ORDER.length : rightIndex

        return leftRank - rightRank || leftName.localeCompare(rightName)
    })
}

function normalizeDoctorName(name) {
    return String(name || '')
        .replace(/^dr\.?\s*/i, '')
        .trim()
}

export function createDoctorPrintAbbreviationMap(doctorNames) {
    const uniqueNames = [...new Set(
        doctorNames.map(normalizeDoctorName).filter(Boolean)
    )]
    const namesByInitial = new Map()

    for (const name of uniqueNames) {
        const initial = name[0].toUpperCase()
        const group = namesByInitial.get(initial) || []
        group.push(name)
        namesByInitial.set(initial, group)
    }

    const abbreviations = new Map()
    for (const [initial, names] of namesByInitial) {
        const orderedNames = [...names].sort(
            (left, right) => left.length - right.length || left.localeCompare(right)
        )
        const usedCodes = new Set()

        for (const name of orderedNames) {
            let code = initial
            for (let length = 1; usedCodes.has(code.toLowerCase()) && length < name.length; length += 1) {
                code = name.slice(0, length + 1)
            }
            usedCodes.add(code.toLowerCase())
            abbreviations.set(name, code)
        }
    }

    return abbreviations
}

export function getDoctorPrintAbbreviation(doctorName, abbreviationMap) {
    const normalizedName = normalizeDoctorName(doctorName)
    return abbreviationMap.get(normalizedName) || normalizedName || 'NO DR'
}

export function buildPrintWeeks(year, monthIndex) {
    const firstDay = new Date(year, monthIndex, 1)
    const lastDay = new Date(year, monthIndex + 1, 0)
    const firstMonday = new Date(firstDay)
    const offsetToMonday = (firstDay.getDay() + 6) % 7
    firstMonday.setDate(firstDay.getDate() - offsetToMonday)

    const weeks = []
    const cursor = new Date(firstMonday)

    while (cursor <= lastDay || cursor.getDay() !== 1) {
        const week = PRINT_WORKDAY_OFFSETS.map((weekdayOffset) => {
            const day = new Date(cursor)
            day.setDate(cursor.getDate() + weekdayOffset)
            return day
        })

        if (week.some((day) => day.getMonth() === monthIndex)) {
            weeks.push(week)
        }

        cursor.setDate(cursor.getDate() + 7)
        if (cursor > lastDay && cursor.getMonth() !== monthIndex) break
    }

    return weeks
}
