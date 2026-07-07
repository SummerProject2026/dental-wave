export function parseLocalDate(dateStr) {
    if (!dateStr) return null

    const [year, month, day] = String(dateStr).split('-').map(Number)
    if (!year || !month || !day) return null

    const date = new Date(year, month - 1, day)
    date.setHours(0, 0, 0, 0)
    return date
}

export function formatDateForInput(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

export function dateRangeContainsSunday(startDateStr, endDateStr = startDateStr) {
    const start = parseLocalDate(startDateStr)
    const end = parseLocalDate(endDateStr || startDateStr)

    if (!start || !end) return false

    const current = new Date(start)
    while (current <= end) {
        if (current.getDay() === 0) {
            return true
        }
        current.setDate(current.getDate() + 1)
    }

    return false
}

export function isPastDateString(dateStr) {
    const date = parseLocalDate(dateStr)
    if (!date) return false

    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return date < today
}
