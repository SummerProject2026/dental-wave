import { useCallback, useState } from 'react'
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { useRouter, useFocusEffect } from 'expo-router'
import { getSession, getUserId } from '@/services/session'
import { getAllNotifications } from '@/services/notifications'

/** Profile icon showing the logged-in user's initial, with a red dot when they have unread notifications. */
export default function ProfileAvatarButton() {
    const router = useRouter()
    const [hasUnread, setHasUnread] = useState(false)

    useFocusEffect(
        useCallback(() => {
            let cancelled = false
            const userId = getUserId()
            if (userId) {
                getAllNotifications(userId)
                    .then((notifications) => {
                        if (!cancelled) setHasUnread(notifications.some((n) => !n.read))
                    })
                    .catch(() => {})
            }
            return () => {
                cancelled = true
            }
        }, [])
    )

    const session = getSession()
    const initials = session?.firstName && session?.lastName
        ? `${session.firstName[0]}${session.lastName[0]}`.toUpperCase()
        : (session?.username?.[0] ?? '?').toUpperCase()

    return (
        <TouchableOpacity style={styles.iconBtn} onPress={() => router.push('/settings')}>
            <View style={styles.avatar}>
                <Text style={styles.avatarText}>{initials}</Text>
            </View>
            {hasUnread && <View style={styles.badge} />}
        </TouchableOpacity>
    )
}

const styles = StyleSheet.create({
    iconBtn: {
        width: 36,
        alignItems: 'center',
        justifyContent: 'center',
    },
    avatar: {
        width: 32,
        height: 32,
        borderRadius: 16,
        backgroundColor: '#443066',
        alignItems: 'center',
        justifyContent: 'center',
    },
    avatarText: {
        color: 'white',
        fontWeight: '700',
        fontSize: 12,
    },
    badge: {
        position: 'absolute',
        top: -2,
        right: 2,
        width: 11,
        height: 11,
        borderRadius: 6,
        backgroundColor: '#e63946',
        borderWidth: 1.5,
        borderColor: '#c8cbff',
    },
})
