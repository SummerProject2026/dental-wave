import { View, Text, TouchableOpacity, StyleSheet, SafeAreaView, ScrollView } from 'react-native'
import { useRouter } from 'expo-router'
import AppHeader from '@/components/app-header'

const menuItems = [
    { label: 'Account', icon: '👤', route: '/profile' },
    { label: 'Notifications', icon: '🔔', route: '/notifications' },
    { label: 'Privacy Notice', icon: '🔒', route: '/privacy-notice' },
    { label: 'Rate our app', icon: '⭐', route: '/rate-app' },
    { label: 'My Requests', icon: '🕐', route: '/my-requests' },
]

export default function SettingsScreen() {
    const router = useRouter()

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack hideProfile />
            <ScrollView contentContainerStyle={styles.content}>

                <View style={styles.card}>
                    {menuItems.map((item, index) => (
                        <View key={item.route}>
                            <TouchableOpacity
                                style={styles.menuItem}
                                onPress={() => router.push(item.route as any)}
                            >
                                <Text style={styles.menuIcon}>{item.icon}</Text>
                                <Text style={styles.menuLabel}>{item.label}</Text>
                                <Text style={styles.menuArrow}>›</Text>
                            </TouchableOpacity>
                            {index < menuItems.length - 1 && <View style={styles.divider} />}
                        </View>
                    ))}
                </View>

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
    menuItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingVertical: 18,
        gap: 12,
    },
    menuIcon: {
        fontSize: 18,
        width: 24,
    },
    menuLabel: {
        flex: 1,
        fontSize: 16,
        color: '#1a1a2e',
        fontWeight: '500',
    },
    menuArrow: {
        fontSize: 22,
        color: '#aaa',
    },
    divider: {
        height: 1,
        backgroundColor: '#eee',
        marginHorizontal: 20,
    },
})
