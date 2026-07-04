import { useState } from 'react'
import {
    View, Text, TextInput, TouchableOpacity, StyleSheet,
    SafeAreaView, ScrollView, KeyboardAvoidingView, Platform, Alert
} from 'react-native'
import { useRouter } from 'expo-router'
import AppHeader from '@/components/app-header'
import { getSession } from '@/services/session'
import { createTimeOffRequest } from '@/services/timeOffRequests'

export default function RequestTimeOffScreen() {
    const router = useRouter()

    const [fromDate, setFromDate] = useState('')
    const [fromTime, setFromTime] = useState('')
    const [toDate, setToDate] = useState('')
    const [toTime, setToTime] = useState('')
    const [reason, setReason] = useState('')
    const [requestType, setRequestType] = useState<'timeoff' | 'emergency'>('timeoff')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    function validate(): boolean {
        if (!fromDate.trim()) { setError('Please enter a start date (YYYY-MM-DD).'); return false }
        const today = new Date(); today.setHours(0, 0, 0, 0)
        const start = new Date(fromDate + 'T00:00:00')
        if (isNaN(start.getTime())) { setError('Invalid start date. Use YYYY-MM-DD format.'); return false }
        if (start < today) { setError('Cannot request time off for a past date.'); return false }
        if (toDate) {
            const end = new Date(toDate + 'T00:00:00')
            if (isNaN(end.getTime())) { setError('Invalid end date. Use YYYY-MM-DD format.'); return false }
            if (end < start) { setError('End date cannot be before start date.'); return false }
        }
        if (fromTime && toTime && fromDate === (toDate || fromDate) && fromTime >= toTime) {
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
                startDate: fromDate.trim(),
                endDate: toDate.trim() || fromDate.trim(),
                startTime: fromTime.trim() ? fromTime.trim() + ':00' : null,
                endTime: toTime.trim() ? toTime.trim() + ':00' : null,
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
                            <TextInput
                                style={styles.dateInput}
                                placeholder="YYYY-MM-DD"
                                placeholderTextColor="#aaa"
                                value={fromDate}
                                onChangeText={setFromDate}
                            />
                            <TextInput
                                style={styles.timeInput}
                                placeholder="HH:MM"
                                placeholderTextColor="#aaa"
                                value={fromTime}
                                onChangeText={setFromTime}
                            />
                        </View>

                        <View style={styles.timeRow}>
                            <Text style={styles.timeRowLabel}>To:</Text>
                            <TextInput
                                style={styles.dateInput}
                                placeholder="YYYY-MM-DD"
                                placeholderTextColor="#aaa"
                                value={toDate}
                                onChangeText={setToDate}
                            />
                            <TextInput
                                style={styles.timeInput}
                                placeholder="HH:MM"
                                placeholderTextColor="#aaa"
                                value={toTime}
                                onChangeText={setToTime}
                            />
                        </View>

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
        paddingHorizontal: 10, paddingVertical: 10, fontSize: 14, color: '#333',
    },
    timeInput: {
        flex: 1, backgroundColor: '#f0f0f8', borderRadius: 8,
        paddingHorizontal: 10, paddingVertical: 10, fontSize: 14, color: '#333',
    },
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
