import { useState, useCallback } from 'react'
import { View, Text, TouchableOpacity, StyleSheet, SafeAreaView, ScrollView, ActivityIndicator, RefreshControl } from 'react-native'
import { useFocusEffect } from 'expo-router'
import AppHeader from '@/components/app-header'
import { getSession } from '@/services/session'
import { getTimeOffRequestsByEmployee, TimeOffRequest } from '@/services/timeOffRequests'

function formatDateRange(req: TimeOffRequest): string {
    const start = req.startDate ?? ''
    const end = req.endDate && req.endDate !== req.startDate ? ` – ${req.endDate}` : ''
    const time = req.startTime
        ? `  ${formatTime(req.startTime)}${req.endTime ? ` - ${formatTime(req.endTime)}` : ''}`
        : ''
    return `${start}${end}${time}`
}

function formatTime(t: string): string {
    const [hStr, mStr] = t.split(':')
    const h = parseInt(hStr, 10)
    const ampm = h >= 12 ? 'PM' : 'AM'
    const h12 = h % 12 || 12
    return `${h12}:${mStr} ${ampm}`
}

export default function MyRequestsScreen() {
    const [requests, setRequests] = useState<TimeOffRequest[]>([])
    const [loading, setLoading] = useState(true)
    const [refreshing, setRefreshing] = useState(false)
    const [error, setError] = useState('')
    const [expandedId, setExpandedId] = useState<number | null>(null)

    const load = useCallback(async () => {
        const session = getSession()
        if (!session?.employeeId) { setError('Not logged in.'); setLoading(false); return }
        try {
            const data = await getTimeOffRequestsByEmployee(session.employeeId)
            setRequests(data)
            setError('')
        } catch (e) {
            setError('Failed to load requests.')
        } finally {
            setLoading(false)
            setRefreshing(false)
        }
    }, [])

    useFocusEffect(useCallback(() => { setLoading(true); load() }, [load]))

    function onRefresh() { setRefreshing(true); load() }

    const pending = requests.filter(r => r.status?.toUpperCase() === 'PENDING')
    const approved = requests.filter(r => r.status?.toUpperCase() === 'APPROVED')
    const rejected = requests.filter(r => r.status?.toUpperCase() === 'DENIED')

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack />
            <ScrollView
                contentContainerStyle={styles.content}
                refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
            >
                <View style={styles.titleRow}>
                    <Text style={styles.title}>My Requests</Text>
                    <View style={styles.dropdown}>
                        <Text style={styles.dropdownText}>Past 30 days ▾</Text>
                    </View>
                </View>

                {loading ? (
                    <ActivityIndicator size="large" color="#443066" style={{ marginTop: 40 }} />
                ) : error ? (
                    <Text style={styles.errorText}>{error}</Text>
                ) : (
                    <>
                        <Text style={styles.sectionLabel}>Pending ⏳</Text>
                        {pending.length === 0
                            ? <Text style={styles.emptyText}>No pending requests.</Text>
                            : pending.map(req => (
                                <RequestCard
                                    key={req.id} req={req}
                                    expanded={expandedId === req.id}
                                    onToggle={() => setExpandedId(prev => prev === req.id ? null : req.id)}
                                />
                            ))
                        }

                        <Text style={styles.sectionLabel}>Approved 🟢</Text>
                        {approved.length === 0
                            ? <Text style={styles.emptyText}>No approved requests.</Text>
                            : approved.map(req => (
                                <RequestCard
                                    key={req.id} req={req}
                                    expanded={expandedId === req.id}
                                    onToggle={() => setExpandedId(prev => prev === req.id ? null : req.id)}
                                />
                            ))
                        }

                        <Text style={styles.sectionLabel}>Rejected 🔴</Text>
                        {rejected.length === 0
                            ? <Text style={styles.emptyText}>No rejected requests.</Text>
                            : rejected.map(req => (
                                <RequestCard
                                    key={req.id} req={req}
                                    expanded={expandedId === req.id}
                                    onToggle={() => setExpandedId(prev => prev === req.id ? null : req.id)}
                                />
                            ))
                        }
                    </>
                )}
            </ScrollView>
        </SafeAreaView>
    )
}

function RequestCard({ req, expanded, onToggle }: { req: TimeOffRequest; expanded: boolean; onToggle: () => void }) {
    const statusColor = req.status?.toUpperCase() === 'APPROVED' ? '#16a34a'
        : req.status?.toUpperCase() === 'DENIED' ? '#dc2626' : '#d97706'

    return (
        <TouchableOpacity style={styles.requestCard} onPress={onToggle} activeOpacity={0.8}>
            <View style={styles.requestHeader}>
                <Text style={styles.requestDate}>{formatDateRange(req)}</Text>
                <Text style={styles.chevron}>{expanded ? '▲' : '▼'}</Text>
            </View>
            {expanded && (
                <View style={styles.requestDetails}>
                    <View style={styles.divider} />
                    {req.reason != null && (
                        <Text style={styles.detailText}>Reason: {req.reason || '—'}</Text>
                    )}
                    <Text style={styles.detailText}>
                        Emergency: {req.emergency ? 'Yes' : 'No'}
                    </Text>
                    {req.submittedAt && (
                        <Text style={styles.detailText}>
                            Submitted: {new Date(req.submittedAt).toLocaleDateString()}
                        </Text>
                    )}
                    {req.reviewedByName && (
                        <Text style={styles.detailText}>Reviewed by: {req.reviewedByName}</Text>
                    )}
                    {req.reviewComment && (
                        <Text style={styles.detailText}>Comment: {req.reviewComment}</Text>
                    )}
                    <Text style={[styles.statusBadge, { color: statusColor }]}>
                        {req.status?.charAt(0).toUpperCase()}{req.status?.slice(1).toLowerCase()}
                    </Text>
                </View>
            )}
        </TouchableOpacity>
    )
}

const styles = StyleSheet.create({
    page: { flex: 1, backgroundColor: '#c8cbff' },
    content: { padding: 20 },
    titleRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 },
    title: { fontSize: 26, fontWeight: 'bold', color: '#1a1a2e' },
    dropdown: { backgroundColor: 'white', borderRadius: 8, paddingHorizontal: 12, paddingVertical: 6 },
    dropdownText: { fontSize: 13, color: '#555' },
    sectionLabel: { fontSize: 17, fontWeight: '700', color: '#1a1a2e', marginBottom: 10, marginTop: 8 },
    emptyText: { fontSize: 14, color: '#888', marginBottom: 8, paddingLeft: 4 },
    errorText: { fontSize: 15, color: '#c0392b', textAlign: 'center', marginTop: 40 },
    requestCard: {
        backgroundColor: 'white', borderRadius: 10, padding: 14, marginBottom: 10,
        shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 4, elevation: 2,
    },
    requestHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
    requestDate: { fontSize: 14, color: '#333', flex: 1 },
    chevron: { fontSize: 14, color: '#888', marginLeft: 8 },
    requestDetails: { marginTop: 10, gap: 4 },
    divider: { height: 1, backgroundColor: '#eee', marginBottom: 8 },
    detailText: { fontSize: 14, color: '#555' },
    statusBadge: { marginTop: 6, fontSize: 14, fontWeight: '700', alignSelf: 'flex-start' },
})
