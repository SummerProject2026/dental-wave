import { useCallback, useState } from 'react'
import {
    View, Text, StyleSheet, SafeAreaView, ScrollView,
    TouchableOpacity, ActivityIndicator, RefreshControl
} from 'react-native'
import { useFocusEffect } from 'expo-router'
import AppHeader from '@/components/app-header'
import { getUserId } from '@/services/session'
import { getAllNotifications, markAsRead, Notification } from '@/services/notifications'

// Picks an icon based on the notification type.
function iconForType(type: string): string {
    switch (type) {
        case 'NEW_SCHEDULE':
            return '📅'
        case 'SCHEDULE_UPDATE':
            return '🔄'
        case 'TIME_OFF_APPROVED':
            return '✅'
        case 'TIME_OFF_DENIED':
            return '❌'
        default:
            return '🔔'
    }
}

// Format ISO datetime -> "6/15/2026, 10:23 AM"
function formatSent(dateTimeStr: string): string {
    if (!dateTimeStr) return ''
    const date = new Date(dateTimeStr)
    if (isNaN(date.getTime())) return dateTimeStr
    return date.toLocaleString([], {
        year: 'numeric', month: 'numeric', day: 'numeric',
        hour: 'numeric', minute: '2-digit',
    })
}

export default function NotificationsScreen() {
    const [notifications, setNotifications] = useState<Notification[]>([])
    const [loading, setLoading] = useState(true)
    const [refreshing, setRefreshing] = useState(false)
    const [error, setError] = useState('')

    const load = useCallback(async () => {
        const userId = getUserId()
        if (!userId) {
            setError('You must be logged in to view notifications.')
            setLoading(false)
            return
        }
        try {
            const data = await getAllNotifications(userId)
            const sorted = [...data].sort(
                (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
            )
            setNotifications(sorted)
            setError('')
        } catch (e) {
            setError('Unable to load notifications.')
        } finally {
            setLoading(false)
            setRefreshing(false)
        }
    }, [])

    // Reload every time the screen comes into focus.
    useFocusEffect(
        useCallback(() => {
            setLoading(true)
            load()
        }, [load])
    )

    function onRefresh() {
        setRefreshing(true)
        load()
    }

    async function handlePress(notification: Notification) {
        if (notification.read) return
        // Optimistically mark as read, then persist.
        setNotifications(prev =>
            prev.map(n => (n.id === notification.id ? { ...n, read: true } : n))
        )
        try {
            await markAsRead(notification.id)
        } catch (e) {
            // Revert on failure
            setNotifications(prev =>
                prev.map(n => (n.id === notification.id ? { ...n, read: false } : n))
            )
        }
    }

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack />
            <ScrollView
                contentContainerStyle={styles.content}
                refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
            >
                <Text style={styles.title}>Notifications</Text>

                {loading ? (
                    <ActivityIndicator size="large" color="#443066" style={styles.loader} />
                ) : error ? (
                    <Text style={styles.errorText}>{error}</Text>
                ) : notifications.length === 0 ? (
                    <Text style={styles.emptyText}>
                        You have no notifications at this time.
                    </Text>
                ) : (
                    <View style={styles.card}>
                        {notifications.map((item, index) => (
                            <View key={item.id}>
                                <TouchableOpacity
                                    style={styles.notifItem}
                                    onPress={() => handlePress(item)}
                                    activeOpacity={0.7}
                                >
                                    <Text style={styles.notifIcon}>{iconForType(item.type)}</Text>
                                    <View style={styles.notifBody}>
                                        <Text style={[styles.notifText, !item.read && styles.notifTextUnread]}>
                                            {item.message}
                                        </Text>
                                        <Text style={styles.notifDate}>{formatSent(item.createdAt)}</Text>
                                    </View>
                                    {!item.read && <View style={styles.unreadDot} />}
                                </TouchableOpacity>
                                {index < notifications.length - 1 && <View style={styles.divider} />}
                            </View>
                        ))}
                    </View>
                )}
            </ScrollView>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    page: {
        flex: 1,
        backgroundColor: '#c8cbff',
    },
    content: {
        padding: 20,
        flexGrow: 1,
    },
    title: {
        fontSize: 26,
        fontWeight: 'bold',
        color: '#1a1a2e',
        marginBottom: 16,
    },
    loader: {
        marginTop: 40,
    },
    errorText: {
        fontSize: 15,
        color: '#c0392b',
        textAlign: 'center',
        marginTop: 40,
    },
    emptyText: {
        fontSize: 15,
        color: '#555',
        textAlign: 'center',
        marginTop: 40,
    },
    card: {
        backgroundColor: 'white',
        borderRadius: 14,
        overflow: 'hidden',
        shadowColor: '#000',
        shadowOpacity: 0.08,
        shadowRadius: 6,
        elevation: 2,
    },
    notifItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingVertical: 16,
        gap: 14,
    },
    notifIcon: {
        fontSize: 22,
    },
    notifBody: {
        flex: 1,
        gap: 4,
    },
    notifText: {
        fontSize: 15,
        color: '#1a1a2e',
    },
    notifTextUnread: {
        fontWeight: '700',
    },
    notifDate: {
        fontSize: 12,
        color: '#888',
    },
    unreadDot: {
        width: 9,
        height: 9,
        borderRadius: 5,
        backgroundColor: '#443066',
    },
    divider: {
        height: 1,
        backgroundColor: '#eee',
        marginHorizontal: 20,
    },
})
