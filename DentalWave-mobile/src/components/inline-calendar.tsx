import { useState } from 'react'
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'

type Props = {
    initialDate: Date
    scheduledDates: Set<string>
    minDate?: Date
    onSelect: (date: Date) => void
}

function dateKey(year: number, month: number, day: number): string {
    const m = String(month + 1).padStart(2, '0')
    const d = String(day).padStart(2, '0')
    return `${year}-${m}-${d}`
}

function isSameDay(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

/** Month-grid date picker that only allows selecting days the employee is scheduled to work. */
export default function InlineCalendar({ initialDate, scheduledDates, minDate, onSelect }: Props) {
    const [viewDate, setViewDate] = useState(new Date(initialDate.getFullYear(), initialDate.getMonth(), 1))

    const year = viewDate.getFullYear()
    const month = viewDate.getMonth()
    const monthLabel = viewDate.toLocaleString('default', { month: 'long' })

    const firstDayOfWeek = viewDate.getDay()
    const daysInMonth = new Date(year, month + 1, 0).getDate()

    const cells: (number | null)[] = []
    for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    const weeks: (number | null)[][] = []
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7))

    const floorMinDate = minDate ? new Date(minDate.getFullYear(), minDate.getMonth(), minDate.getDate()) : null

    function isDisabled(day: number | null): boolean {
        if (!day) return true
        const date = new Date(year, month, day)
        const dow = date.getDay()
        if (dow === 0 || dow === 6) return true
        if (floorMinDate && date < floorMinDate) return true
        return !scheduledDates.has(dateKey(year, month, day))
    }

    return (
        <View style={styles.panel}>
            <View style={styles.nav}>
                <TouchableOpacity onPress={() => setViewDate(new Date(year, month - 1, 1))}>
                    <Text style={styles.navArrow}>‹</Text>
                </TouchableOpacity>
                <Text style={styles.monthLabel}>{monthLabel} {year}</Text>
                <TouchableOpacity onPress={() => setViewDate(new Date(year, month + 1, 1))}>
                    <Text style={styles.navArrow}>›</Text>
                </TouchableOpacity>
            </View>

            <View style={styles.dayHeaders}>
                {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(d => (
                    <Text key={d} style={styles.dayHeader}>{d}</Text>
                ))}
            </View>

            {weeks.map((week, wi) => (
                <View key={wi} style={styles.week}>
                    {week.map((day, di) => {
                        const disabled = isDisabled(day)
                        const selected = day ? isSameDay(new Date(year, month, day), initialDate) : false
                        return (
                            <TouchableOpacity
                                key={di}
                                style={[
                                    styles.dayCell,
                                    selected && styles.selectedCell,
                                ]}
                                disabled={disabled}
                                onPress={() => day && onSelect(new Date(year, month, day))}
                            >
                                <Text style={[
                                    styles.dayText,
                                    disabled && styles.disabledText,
                                    selected && styles.selectedText,
                                ]}>
                                    {day || ''}
                                </Text>
                            </TouchableOpacity>
                        )
                    })}
                </View>
            ))}

            <Text style={styles.legend}>Only days you're scheduled to work can be selected.</Text>
        </View>
    )
}

const styles = StyleSheet.create({
    panel: { backgroundColor: '#f0f0f8', borderRadius: 10, padding: 12 },
    nav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 },
    navArrow: { fontSize: 22, color: '#443066', paddingHorizontal: 10 },
    monthLabel: { fontSize: 15, fontWeight: 'bold', color: '#333' },
    dayHeaders: { flexDirection: 'row', marginBottom: 4 },
    dayHeader: { flex: 1, textAlign: 'center', fontSize: 12, color: '#888', fontWeight: 'bold' },
    week: { flexDirection: 'row' },
    dayCell: { flex: 1, alignItems: 'center', paddingVertical: 8, borderRadius: 16 },
    selectedCell: { backgroundColor: '#443066' },
    dayText: { fontSize: 14, color: '#333' },
    disabledText: { color: '#ccc' },
    selectedText: { color: 'white', fontWeight: 'bold' },
    legend: { fontSize: 11, color: '#888', textAlign: 'center', marginTop: 8 },
})
