import { useState, useCallback } from 'react'
import {
    View, Text, TouchableOpacity, StyleSheet,
    Image, ScrollView, SafeAreaView, ActivityIndicator
} from 'react-native'
import { useRouter, useFocusEffect } from 'expo-router'
import { getSession } from '@/services/session'
import { getSchedulesByEmployee, getAllCalendars, ScheduleEntry } from '@/services/schedules'

const OFFICE_NAMES: Record<number, string> = { 1: 'Raleigh', 2: 'Garner', 3: 'Smithfield' }

function formatTime(t: string | null): string {
    if (!t) return ''
    const [hStr, mStr] = t.split(':')
    const h = parseInt(hStr, 10)
    const m = mStr
    const ampm = h >= 12 ? 'pm' : 'am'
    const h12 = h % 12 || 12
    return `${h12}:${m} ${ampm}`
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
    const [scheduleMap, setScheduleMap] = useState<Record<string, ScheduleEntry>>({})
    const [officeMap, setOfficeMap] = useState<Record<number, string>>({})
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const year = currentDate.getFullYear()
    const month = currentDate.getMonth()
    const monthName = currentDate.toLocaleString('default', { month: 'short' })
    const fullMonthName = currentDate.toLocaleString('default', { month: 'long' })

    const firstDayOfWeek = currentDate.getDay()
    const daysInMonth = new Date(year, month + 1, 0).getDate()

    const cells: (number | null)[] = []
    for (let i = 0; i < firstDayOfWeek; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    while (cells.length % 7 !== 0) cells.push(null)

    const weeks: (number | null)[][] = []
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7))

    useFocusEffect(
        useCallback(() => {
            loadData()
        }, [])
    )

    async function loadData() {
        const session = getSession()
        if (!session?.employeeId) {
            setError('Not logged in.')
            setLoading(false)
            return
        }
        setLoading(true)
        setError('')
        try {
            const [schedules, calendars] = await Promise.all([
                getSchedulesByEmployee(session.employeeId),
                getAllCalendars(),
            ])
            const newScheduleMap: Record<string, ScheduleEntry> = {}
            for (const s of schedules) {
                if (s.date) newScheduleMap[s.date] = s
            }
            const newOfficeMap: Record<number, string> = {}
            for (const cal of calendars) {
                if (cal.id && cal.officeId) {
                    newOfficeMap[cal.id] = OFFICE_NAMES[cal.officeId] ?? `Office ${cal.officeId}`
                }
            }
            setScheduleMap(newScheduleMap)
            setOfficeMap(newOfficeMap)
        } catch (e) {
            setError('Failed to load schedule.')
        } finally {
            setLoading(false)
        }
    }

    function isToday(day: number | null) {
        return day === today.getDate() && month === today.getMonth() && year === today.getFullYear()
    }

    function dateKey(day: number) {
        const m = String(month + 1).padStart(2, '0')
        const d = String(day).padStart(2, '0')
        return `${year}-${m}-${d}`
    }

    const selectedKey = selectedDay ? dateKey(selectedDay) : null
    const selectedSchedule = selectedKey ? scheduleMap[selectedKey] : null
    const selectedDateLabel = selectedDay ? `${fullMonthName} ${getOrdinal(selectedDay)}, ${year}` : null

    return (
        <SafeAreaView style={styles.page}>

            <View style={styles.header}>
                <TouchableOpacity style={styles.iconBtn}>
                    <Text style={styles.icon}>☰</Text>
                </TouchableOpacity>
                <Image
                    source={require('../../assets/images/wake-logo.png')}
                    style={styles.headerLogo}
                    resizeMode="contain"
                />
                <TouchableOpacity style={styles.iconBtn} onPress={() => router.push('/settings')}>
                    <Text style={styles.icon}>👤</Text>
                </TouchableOpacity>
            </View>

            <ScrollView contentContainerStyle={styles.content}>

                {loading ? (
                    <ActivityIndicator size="large" color="#443066" style={{ marginTop: 40 }} />
                ) : error ? (
                    <Text style={styles.errorText}>{error}</Text>
                ) : (
                    <>
                        <View style={styles.calendarCard}>

                            <View style={styles.calendarNav}>
                                <TouchableOpacity onPress={() => {
                                    setCurrentDate(new Date(year, month - 1, 1))
                                    setSelectedDay(null)
                                }}>
                                    <Text style={styles.navArrow}>‹</Text>
                                </TouchableOpacity>
                                <Text style={styles.monthLabel}>{monthName}</Text>
                                <Text style={styles.yearLabel}>{year}</Text>
                                <TouchableOpacity onPress={() => {
                                    setCurrentDate(new Date(year, month + 1, 1))
                                    setSelectedDay(null)
                                }}>
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
                                        const hasSchedule = day ? !!scheduleMap[dateKey(day)] : false
                                        return (
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
                                                {hasSchedule && <View style={styles.scheduleDot} />}
                                            </TouchableOpacity>
                                        )
                                    })}
                                </View>
                            ))}

                        </View>

                        {selectedDay && (
                            <>
                                <Text style={styles.selectedDateTitle}>{selectedDateLabel}</Text>

                                <View style={styles.scheduleCard}>
                                    {selectedSchedule ? (
                                        <>
                                            <Text style={styles.scheduleText}>
                                                Time: {formatTime(selectedSchedule.startTime)} - {formatTime(selectedSchedule.endTime)}
                                            </Text>
                                            {selectedSchedule.calendarId && officeMap[selectedSchedule.calendarId] && (
                                                <Text style={styles.scheduleText}>
                                                    Office: {officeMap[selectedSchedule.calendarId]}
                                                </Text>
                                            )}
                                            {selectedSchedule.notes ? (
                                                <Text style={styles.scheduleText}>
                                                    Notes: {selectedSchedule.notes}
                                                </Text>
                                            ) : null}
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
                    </>
                )}

                <Text style={styles.footer}>© All Rights Reserved</Text>

            </ScrollView>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    page: { flex: 1, backgroundColor: '#c8cbff' },
    header: {
        flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
        paddingHorizontal: 16, paddingVertical: 8, backgroundColor: '#c8cbff',
    },
    iconBtn: { width: 36, alignItems: 'center' },
    icon: { fontSize: 26, color: '#1a1a2e' },
    headerLogo: { width: 180, height: 60 },
    errorText: { fontSize: 15, color: '#c0392b', textAlign: 'center', marginTop: 40 },
    content: { padding: 16, alignItems: 'center' },
    calendarCard: {
        backgroundColor: 'white', borderRadius: 16, padding: 16, width: '100%',
        shadowColor: '#000', shadowOpacity: 0.1, shadowRadius: 8, elevation: 3, marginBottom: 20,
    },
    calendarNav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 },
    navArrow: { fontSize: 24, color: '#443066', paddingHorizontal: 8 },
    monthLabel: { fontSize: 16, fontWeight: 'bold', color: '#333', flex: 1, textAlign: 'center' },
    yearLabel: { fontSize: 16, fontWeight: 'bold', color: '#333', flex: 1, textAlign: 'center' },
    dayHeaders: { flexDirection: 'row', marginBottom: 4 },
    dayHeader: { flex: 1, textAlign: 'center', fontSize: 13, color: '#888', fontWeight: 'bold' },
    week: { flexDirection: 'row' },
    dayCell: { flex: 1, alignItems: 'center', paddingVertical: 8, borderRadius: 20 },
    todayCell: { backgroundColor: '#1a1a2e', borderRadius: 20 },
    selectedCell: { backgroundColor: '#dde0ff', borderRadius: 20 },
    dayText: { fontSize: 14, color: '#333' },
    todayText: { color: 'white', fontWeight: 'bold' },
    selectedText: { color: '#2444d8', fontWeight: 'bold' },
    emptyText: { color: '#ccc' },
    scheduleDot: { width: 5, height: 5, borderRadius: 3, backgroundColor: '#2444d8', marginTop: 2 },
    selectedDateTitle: { fontSize: 26, fontWeight: 'bold', color: '#1a1a2e', textAlign: 'center', marginBottom: 12 },
    scheduleCard: {
        backgroundColor: 'white', borderRadius: 10, borderWidth: 1, borderColor: '#ccc',
        padding: 16, width: '100%', minHeight: 100, marginBottom: 20,
    },
    scheduleText: { fontSize: 16, color: '#222', marginBottom: 6 },
    notScheduledText: { fontSize: 16, color: '#888' },
    requestBtn: {
        backgroundColor: '#443066', borderRadius: 10, paddingVertical: 16,
        paddingHorizontal: 32, width: '80%', alignItems: 'center', marginBottom: 20,
    },
    requestBtnText: { color: 'white', fontSize: 18, fontWeight: 'bold' },
    footer: { color: '#888', fontSize: 14, marginTop: 8 },
})
