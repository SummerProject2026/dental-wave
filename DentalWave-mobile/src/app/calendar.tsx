import { useState } from 'react'
import {
    View, Text, TouchableOpacity, StyleSheet,
    Image, ScrollView, SafeAreaView
} from 'react-native'
import { useRouter } from 'expo-router'

// Placeholder schedule - replace with API later
const mockSchedule: Record<string, { time: string; office: string; notes: string }> = {
    '2026-6-14': { time: '8:00 am - 5:00 pm', office: 'Raleigh', notes: 'Doctor assigned' },
    '2026-6-16': { time: '9:00 am - 3:00 pm', office: 'Garner', notes: 'Team A' },
}

function getOrdinal(n: number) {
    const s = ['th', 'st', 'nd', 'rd']
    const v = n % 100
    return n + (s[(v - 20) % 10] || s[v] || s[0])
}

export default function CalendarScreen() {
    const router = useRouter()
    const today = new Date()
    const [currentDate, setCurrentDate] = useState(
        new Date(today.getFullYear(), today.getMonth(), 1)
    )
    const [selectedDay, setSelectedDay] = useState<number | null>(null)

    const year = currentDate.getFullYear()
    const month = currentDate.getMonth() + 1
    const monthName = currentDate.toLocaleString('default', { month: 'short' })
    const fullMonthName = currentDate.toLocaleString('default', { month: 'long' })

    const firstDayOfWeek = currentDate.getDay()
    const daysInMonth = new Date(year, currentDate.getMonth() + 1, 0).getDate()

    const cells: (number | null)[] = []
    for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    const weeks = []
    for (let i = 0; i < cells.length; i += 7) {
        weeks.push(cells.slice(i, i + 7))
    }

    function isToday(day: number | null) {
        return day === today.getDate() &&
            currentDate.getMonth() === today.getMonth() &&
            currentDate.getFullYear() === today.getFullYear()
    }

    function getScheduleKey(day: number) {
        return `${year}-${month}-${day}`
    }

    const selectedSchedule = selectedDay ? mockSchedule[getScheduleKey(selectedDay)] : null

    const selectedDateLabel = selectedDay
        ? `${fullMonthName} ${getOrdinal(selectedDay)}, ${year}`
        : null

    return (
        <SafeAreaView style={styles.page}>

            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity>
                    <Text style={styles.menuIcon}>☰</Text>
                </TouchableOpacity>
                <Image
                    source={require('../../assets/images/wake-logo.png')}
                    style={styles.headerLogo}
                    resizeMode="contain"
                />
                <TouchableOpacity onPress={() => router.push('/profigit statule')}>
                    <Text style={styles.profileIcon}>👤</Text>
                </TouchableOpacity>
            </View>

            <ScrollView contentContainerStyle={styles.content}>

                {/* Calendar card */}
                <View style={styles.calendarCard}>

                    {/* Month navigation */}
                    <View style={styles.calendarNav}>
                        <TouchableOpacity onPress={() =>
                            setCurrentDate(new Date(year, currentDate.getMonth() - 1, 1))
                        }>
                            <Text style={styles.navArrow}>‹</Text>
                        </TouchableOpacity>

                        <Text style={styles.monthLabel}>{monthName}</Text>
                        <Text style={styles.yearLabel}>{year}</Text>

                        <TouchableOpacity onPress={() =>
                            setCurrentDate(new Date(year, currentDate.getMonth() + 1, 1))
                        }>
                            <Text style={styles.navArrow}>›</Text>
                        </TouchableOpacity>
                    </View>

                    {/* Day headers */}
                    <View style={styles.dayHeaders}>
                        {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(d => (
                            <Text key={d} style={styles.dayHeader}>{d}</Text>
                        ))}
                    </View>

                    {/* Calendar grid */}
                    {weeks.map((week, wi) => (
                        <View key={wi} style={styles.week}>
                            {week.map((day, di) => (
                                <TouchableOpacity
                                    key={di}
                                    style={[
                                        styles.dayCell,
                                        isToday(day) && styles.todayCell,
                                        day === selectedDay && styles.selectedCell,
                                    ]}
                                    onPress={() => day && setSelectedDay(day)}
                                    disabled={!day}
                                >
                                    <Text style={[
                                        styles.dayText,
                                        isToday(day) && styles.todayText,
                                        day === selectedDay && styles.selectedText,
                                        !day && styles.emptyText,
                                    ]}>
                                        {day || ''}
                                    </Text>
                                    {day && mockSchedule[getScheduleKey(day)] && (
                                        <View style={styles.scheduleDot} />
                                    )}
                                </TouchableOpacity>
                            ))}
                        </View>
                    ))}

                </View>

                {/* Selected day info */}
                {selectedDay && (
                    <>
                        <Text style={styles.selectedDateTitle}>{selectedDateLabel}</Text>

                        <View style={styles.scheduleCard}>
                            {selectedSchedule ? (
                                <>
                                    <Text style={styles.scheduleText}>
                                        Time: {selectedSchedule.time}
                                    </Text>
                                    <Text style={styles.scheduleText}>
                                        Office: {selectedSchedule.office}
                                    </Text>
                                    <Text style={styles.scheduleText}>
                                        Notes: {selectedSchedule.notes}
                                    </Text>
                                </>
                            ) : (
                                <Text style={styles.notScheduledText}>Not Scheduled</Text>
                            )}
                        </View>

                        <TouchableOpacity
                            style={styles.requestBtn}
                            onPress={() => router.push('/request-time-off')}
                        >
                            <Text style={styles.requestBtnText}>Request Time Off</Text>
                        </TouchableOpacity>
                    </>
                )}

                <Text style={styles.footer}>©</Text>

            </ScrollView>

        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    page: {
        flex: 1,
        backgroundColor: '#c8cbff',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 16,
        paddingVertical: 8,
        backgroundColor: '#c8cbff',
    },
    menuIcon: {
        fontSize: 26,
        color: '#1a1a2e',
    },
    headerLogo: {
        width: 180,
        height: 60,
    },
    profileIcon: {
        fontSize: 26,
    },
    content: {
        padding: 16,
        alignItems: 'center',
    },
    calendarCard: {
        backgroundColor: 'white',
        borderRadius: 16,
        padding: 16,
        width: '100%',
        shadowColor: '#000',
        shadowOpacity: 0.1,
        shadowRadius: 8,
        elevation: 3,
        marginBottom: 20,
    },
    calendarNav: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 12,
    },
    navArrow: {
        fontSize: 24,
        color: '#443066',
        paddingHorizontal: 8,
    },
    monthLabel: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        flex: 1,
        textAlign: 'center',
    },
    yearLabel: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        flex: 1,
        textAlign: 'center',
    },
    dayHeaders: {
        flexDirection: 'row',
        marginBottom: 4,
    },
    dayHeader: {
        flex: 1,
        textAlign: 'center',
        fontSize: 13,
        color: '#888',
        fontWeight: 'bold',
    },
    week: {
        flexDirection: 'row',
    },
    dayCell: {
        flex: 1,
        alignItems: 'center',
        paddingVertical: 8,
        borderRadius: 20,
    },
    todayCell: {
        backgroundColor: '#1a1a2e',
        borderRadius: 20,
    },
    selectedCell: {
        backgroundColor: '#dde0ff',
        borderRadius: 20,
    },
    dayText: {
        fontSize: 14,
        color: '#333',
    },
    todayText: {
        color: 'white',
        fontWeight: 'bold',
    },
    selectedText: {
        color: '#2444d8',
        fontWeight: 'bold',
    },
    emptyText: {
        color: '#ccc',
    },
    scheduleDot: {
        width: 5,
        height: 5,
        borderRadius: 3,
        backgroundColor: '#2444d8',
        marginTop: 2,
    },
    selectedDateTitle: {
        fontSize: 26,
        fontWeight: 'bold',
        color: '#1a1a2e',
        textAlign: 'center',
        marginBottom: 12,
    },
    scheduleCard: {
        backgroundColor: 'white',
        borderRadius: 10,
        borderWidth: 1,
        borderColor: '#ccc',
        padding: 16,
        width: '100%',
        minHeight: 100,
        marginBottom: 20,
    },
    scheduleText: {
        fontSize: 16,
        color: '#222',
        marginBottom: 6,
    },
    notScheduledText: {
        fontSize: 16,
        color: '#888',
    },
    requestBtn: {
        backgroundColor: '#443066',
        borderRadius: 10,
        paddingVertical: 16,
        paddingHorizontal: 32,
        width: '80%',
        alignItems: 'center',
        marginBottom: 20,
    },
    requestBtnText: {
        color: 'white',
        fontSize: 18,
        fontWeight: 'bold',
    },
    footer: {
        color: '#888',
        fontSize: 14,
        marginTop: 8,
    },
})