import { useState, useCallback } from 'react'
import {
    View, Text, TextInput, TouchableOpacity, StyleSheet,
    SafeAreaView, ScrollView, KeyboardAvoidingView, Platform, Alert
} from 'react-native'
import { useRouter, useFocusEffect } from 'expo-router'
import DateTimePicker, { DateTimePickerEvent } from '@react-native-community/datetimepicker'
import AppHeader from '@/components/app-header'
import InlineCalendar from '@/components/inline-calendar'
import { getSession } from '@/services/session'
import { createTimeOffRequest } from '@/services/timeOffRequests'
import { getSchedulesByEmployee } from '@/services/schedules'

type PickerTarget = 'fromDate' | 'fromTime' | 'toDate' | 'toTime' | null

function toDateStr(d: Date): string {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
}

function toTimeStr(d: Date): string {
    const h = String(d.getHours()).padStart(2, '0')
    const m = String(d.getMinutes()).padStart(2, '0')
    return `${h}:${m}:00`
}

function formatDateDisplay(d: Date | null): string {
    if (!d) return 'Select date'
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

function formatTimeDisplay(d: Date | null): string {
    if (!d) return 'Select time'
    return d.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit', hour12: true })
}

export default function RequestTimeOffScreen() {
    const router = useRouter()

    const [fromDate, setFromDate] = useState<Date | null>(null)
    const [fromTime, setFromTime] = useState<Date | null>(null)
    const [toDate, setToDate] = useState<Date | null>(null)
    const [toTime, setToTime] = useState<Date | null>(null)
    const [activePicker, setActivePicker] = useState<PickerTarget>(null)
    const [reason, setReason] = useState('')
    const [requestType, setRequestType] = useState<'timeoff' | 'emergency'>('timeoff')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [scheduledDates, setScheduledDates] = useState<Set<string>>(new Set())
    const [scheduleLoaded, setScheduleLoaded] = useState(false)

    useFocusEffect(
        useCallback(() => {
            const session = getSession()
            if (!session?.employeeId) return
            getSchedulesByEmployee(session.employeeId)
                .then((schedules) => {
                    setScheduledDates(new Set(schedules.map((s) => s.date).filter(Boolean)))
                })
                .catch(() => {})
                .finally(() => setScheduleLoaded(true))
        }, [])
    )

    function valueForTimePicker(target: PickerTarget): Date {
        switch (target) {
            case 'fromTime': return fromTime ?? new Date()
            case 'toTime': return toTime ?? new Date()
            default: return new Date()
        }
    }

    function handleTimePickerChange(event: DateTimePickerEvent, selected?: Date) {
        if (Platform.OS === 'android') setActivePicker(null)
        if (event.type === 'dismissed' || !selected) return
        if (activePicker === 'fromTime') setFromTime(selected)
        else if (activePicker === 'toTime') setToTime(selected)
    }

    function handleDateSelect(target: PickerTarget, date: Date) {
        if (target === 'fromDate') setFromDate(date)
        else if (target === 'toDate') setToDate(date)
        setActivePicker(null)
    }

    function openPicker(target: PickerTarget) {
        setError('')
        setActivePicker(target)
    }

    function validate(): boolean {
        if (!fromDate) { setError('Please select a start date.'); return false }
        if (toDate && toDate < fromDate) { setError('End date cannot be before start date.'); return false }
        if (fromTime && toTime && toDateStr(fromDate) === toDateStr(toDate ?? fromDate) && toTimeStr(fromTime) >= toTimeStr(toTime)) {
            setError('End time must be after start time.')
            return false
        }
        return true
    }

    async function handleSubmit() {
        setError('')
        if (!validate()) return

        const session = getSession()
        if (!session?.employeeId) { setError('Not logged in.'); return }

        setSubmitting(true)
        try {
            await createTimeOffRequest({
                employeeId: session.employeeId,
                startDate: toDateStr(fromDate!),
                endDate: toDateStr(toDate ?? fromDate!),
                startTime: fromTime ? toTimeStr(fromTime) : null,
                endTime: toTime ? toTimeStr(toTime) : null,
                reason: reason.trim(),
                emergency: requestType === 'emergency',
                status: 'PENDING',
            })
            Alert.alert('Success', 'Your time-off request has been submitted.', [
                { text: 'OK', onPress: () => router.replace('/my-requests') }
            ])
        } catch (e) {
            setError('Failed to submit request. Please try again.')
        } finally {
            setSubmitting(false)
        }
    }

    const isDatePicker = activePicker === 'fromDate' || activePicker === 'toDate'
    const isTimePicker = activePicker === 'fromTime' || activePicker === 'toTime'

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack />
            <KeyboardAvoidingView
                style={{ flex: 1 }}
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
            >
                <ScrollView contentContainerStyle={styles.content}>

                    <Text style={styles.title}>Request Time off</Text>

                    <View style={styles.card}>

                        <Text style={styles.sectionLabel}>Enter time:</Text>

                        <View style={styles.timeRow}>
                            <Text style={styles.timeRowLabel}>From:</Text>
                            <TouchableOpacity style={styles.dateInput} onPress={() => openPicker('fromDate')}>
                                <Text style={[styles.fieldText, !fromDate && styles.placeholderText]}>
                                    {formatDateDisplay(fromDate)}
                                </Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={styles.timeInput} onPress={() => openPicker('fromTime')}>
                                <Text style={[styles.fieldText, !fromTime && styles.placeholderText]}>
                                    {formatTimeDisplay(fromTime)}
                                </Text>
                            </TouchableOpacity>
                        </View>

                        <View style={styles.timeRow}>
                            <Text style={styles.timeRowLabel}>To:</Text>
                            <TouchableOpacity style={styles.dateInput} onPress={() => openPicker('toDate')}>
                                <Text style={[styles.fieldText, !toDate && styles.placeholderText]}>
                                    {formatDateDisplay(toDate)}
                                </Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={styles.timeInput} onPress={() => openPicker('toTime')}>
                                <Text style={[styles.fieldText, !toTime && styles.placeholderText]}>
                                    {formatTimeDisplay(toTime)}
                                </Text>
                            </TouchableOpacity>
                        </View>

                        {isDatePicker && (
                            scheduleLoaded ? (
                                <InlineCalendar
                                    initialDate={(activePicker === 'toDate' ? toDate : fromDate) ?? fromDate ?? new Date()}
                                    scheduledDates={scheduledDates}
                                    minDate={activePicker === 'toDate' ? (fromDate ?? new Date()) : new Date()}
                                    onSelect={(date) => handleDateSelect(activePicker, date)}
                                />
                            ) : (
                                <Text style={styles.fieldText}>Loading your schedule...</Text>
                            )
                        )}

                        {isTimePicker && Platform.OS === 'ios' && (
                            <View style={styles.pickerPanel}>
                                <View style={styles.pickerPanelHeader}>
                                    <TouchableOpacity onPress={() => setActivePicker(null)}>
                                        <Text style={styles.pickerDoneText}>Done</Text>
                                    </TouchableOpacity>
                                </View>
                                <DateTimePicker
                                    value={valueForTimePicker(activePicker)}
                                    mode="time"
                                    display="spinner"
                                    is24Hour={false}
                                    locale="en-US"
                                    onChange={handleTimePickerChange}
                                />
                            </View>
                        )}

                        {isTimePicker && Platform.OS === 'android' && (
                            <DateTimePicker
                                value={valueForTimePicker(activePicker)}
                                mode="time"
                                display="default"
                                is24Hour={false}
                                onChange={handleTimePickerChange}
                            />
                        )}

                        <Text style={styles.sectionLabel}>Reason:</Text>
                        <TextInput
                            style={styles.reasonInput}
                            value={reason}
                            onChangeText={setReason}
                            multiline
                            numberOfLines={4}
                            placeholder="Enter reason..."
                            placeholderTextColor="#aaa"
                            textAlignVertical="top"
                        />

                        <Text style={styles.sectionLabel}>Request Type:</Text>
                        <View style={styles.typeRow}>
                            <TouchableOpacity
                                style={[
                                    styles.typeBtn,
                                    requestType === 'timeoff' && styles.typeBtnActive,
                                ]}
                                onPress={() => setRequestType('timeoff')}
                            >
                                <Text style={[
                                    styles.typeBtnText,
                                    requestType === 'timeoff' && styles.typeBtnTextActive,
                                ]}>Time Off</Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={[
                                    styles.typeBtn,
                                    styles.typeBtnEmergency,
                                    requestType === 'emergency' && styles.typeBtnEmergencyActive,
                                ]}
                                onPress={() => setRequestType('emergency')}
                            >
                                <Text style={[
                                    styles.typeBtnText,
                                    requestType === 'emergency' && styles.typeBtnTextActive,
                                ]}>Emergency</Text>
                            </TouchableOpacity>
                        </View>

                        {error ? <Text style={styles.errorText}>{error}</Text> : null}

                        <TouchableOpacity
                            style={[styles.submitBtn, submitting && styles.submitBtnDisabled]}
                            onPress={handleSubmit}
                            disabled={submitting}
                        >
                            <Text style={styles.submitBtnText}>
                                {submitting ? 'Submitting...' : 'Submit'}
                            </Text>
                        </TouchableOpacity>

                    </View>

                </ScrollView>
            </KeyboardAvoidingView>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    page: { flex: 1, backgroundColor: '#c8cbff' },
    content: { padding: 20 },
    title: { fontSize: 26, fontWeight: 'bold', color: '#1a1a2e', marginBottom: 16 },
    card: {
        backgroundColor: 'white', borderRadius: 14, padding: 20,
        shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 6, elevation: 2, gap: 12,
    },
    sectionLabel: { fontSize: 15, fontWeight: '600', color: '#1a1a2e' },
    timeRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
    timeRowLabel: { fontSize: 15, color: '#444', width: 36 },
    dateInput: {
        flex: 2, backgroundColor: '#f0f0f8', borderRadius: 8,
        paddingHorizontal: 10, paddingVertical: 12, justifyContent: 'center',
    },
    timeInput: {
        flex: 1, backgroundColor: '#f0f0f8', borderRadius: 8,
        paddingHorizontal: 10, paddingVertical: 12, justifyContent: 'center',
    },
    fieldText: { fontSize: 14, color: '#333' },
    placeholderText: { color: '#aaa' },
    pickerPanel: {
        backgroundColor: '#f0f0f8', borderRadius: 10, overflow: 'hidden',
    },
    pickerPanelHeader: {
        flexDirection: 'row', justifyContent: 'flex-end',
        paddingHorizontal: 14, paddingTop: 8,
    },
    pickerDoneText: { color: '#443066', fontWeight: '700', fontSize: 15 },
    reasonInput: {
        backgroundColor: '#f0f0f8', borderRadius: 8,
        paddingHorizontal: 12, paddingVertical: 10,
        fontSize: 14, color: '#333', minHeight: 90,
    },
    typeRow: { flexDirection: 'row', gap: 12 },
    typeBtn: {
        flex: 1, paddingVertical: 12, borderRadius: 24, alignItems: 'center',
        backgroundColor: '#e8e8f0', borderWidth: 1, borderColor: '#ccc',
    },
    typeBtnActive: { backgroundColor: '#443066', borderColor: '#443066' },
    typeBtnEmergency: { borderColor: '#e53935' },
    typeBtnEmergencyActive: { backgroundColor: '#e53935', borderColor: '#e53935' },
    typeBtnText: { fontSize: 15, fontWeight: '600', color: '#555' },
    typeBtnTextActive: { color: 'white' },
    errorText: { fontSize: 14, color: '#c0392b' },
    submitBtn: {
        backgroundColor: '#443066', borderRadius: 10, paddingVertical: 16, alignItems: 'center', marginTop: 4,
    },
    submitBtnDisabled: { opacity: 0.6 },
    submitBtnText: { color: 'white', fontSize: 16, fontWeight: 'bold' },
})
