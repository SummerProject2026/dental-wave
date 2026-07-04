import { useState, useCallback } from 'react'
import {
    View, Text, StyleSheet, SafeAreaView, ScrollView,
    TouchableOpacity, ActivityIndicator
} from 'react-native'
import { useRouter, useFocusEffect } from 'expo-router'
import AppHeader from '@/components/app-header'
import { getSession, clearSession } from '@/services/session'
import { getEmployeeById, EmployeeProfile } from '@/services/employee'

function InfoRow({ label, value }: { label: string; value: string }) {
    return (
        <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>{label}</Text>
            <View style={styles.infoValueBox}>
                <Text style={styles.infoValue}>{value}</Text>
            </View>
        </View>
    )
}

export default function ProfileScreen() {
    const router = useRouter()
    const [profile, setProfile] = useState<EmployeeProfile | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useFocusEffect(
        useCallback(() => {
            loadProfile()
        }, [])
    )

    async function loadProfile() {
        const session = getSession()
        if (!session?.employeeId) {
            setError('Not logged in.')
            setLoading(false)
            return
        }
        setLoading(true)
        setError('')
        try {
            const data = await getEmployeeById(session.employeeId)
            setProfile(data)
        } catch (e) {
            setError('Failed to load profile.')
        } finally {
            setLoading(false)
        }
    }

    function handleLogout() {
        clearSession()
        router.replace('/')
    }

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack hideProfile />
            <ScrollView contentContainerStyle={styles.content}>

                {loading ? (
                    <ActivityIndicator size="large" color="#443066" style={{ marginTop: 40 }} />
                ) : error ? (
                    <Text style={styles.errorText}>{error}</Text>
                ) : profile ? (
                    <>
                        <View style={styles.avatarCard}>
                            <Text style={styles.avatarIcon}>🦷</Text>
                            <Text style={styles.avatarName}>{profile.firstName} {profile.lastName}</Text>
                            <Text style={styles.avatarUsername}>{profile.username}</Text>
                        </View>

                        <View style={styles.infoCard}>
                            <InfoRow label="Name:" value={`${profile.firstName} ${profile.lastName}`} />
                            <InfoRow label="Email:" value={profile.email} />
                            <InfoRow label="Phone number:" value={profile.phoneNumber ?? '—'} />
                            <InfoRow label="Status:" value={profile.status} />
                            <InfoRow label="Hire Date:" value={profile.hireDate} />
                            <InfoRow label="PTO:" value={`${profile.timeOff ?? 0} hrs`} />
                            <InfoRow label="Position:" value={profile.position} />
                        </View>

                        <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
                            <Text style={styles.logoutText}>Log out</Text>
                        </TouchableOpacity>
                    </>
                ) : null}

            </ScrollView>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    page: { flex: 1, backgroundColor: '#c8cbff' },
    content: { padding: 20, alignItems: 'center' },
    errorText: { fontSize: 15, color: '#c0392b', textAlign: 'center', marginTop: 40 },
    avatarCard: {
        backgroundColor: 'white', borderRadius: 14, padding: 24, width: '100%',
        alignItems: 'center', marginBottom: 16,
        shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 6, elevation: 2,
    },
    avatarIcon: { fontSize: 48, marginBottom: 8 },
    avatarName: { fontSize: 20, fontWeight: 'bold', color: '#1a1a2e' },
    avatarUsername: { fontSize: 15, color: '#666', marginTop: 4 },
    infoCard: {
        backgroundColor: 'white', borderRadius: 14, padding: 20, width: '100%',
        marginBottom: 24, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 6, elevation: 2,
        gap: 12,
    },
    infoRow: { gap: 4 },
    infoLabel: { fontSize: 14, fontWeight: '600', color: '#1a1a2e' },
    infoValueBox: {
        backgroundColor: '#f0f0f8', borderRadius: 8, paddingHorizontal: 12, paddingVertical: 10,
    },
    infoValue: { fontSize: 15, color: '#333' },
    logoutBtn: {
        backgroundColor: '#443066', borderRadius: 10, paddingVertical: 16,
        paddingHorizontal: 48, alignItems: 'center', marginBottom: 20,
    },
    logoutText: { color: 'white', fontSize: 16, fontWeight: 'bold' },
})
