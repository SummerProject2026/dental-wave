import { View, Text, StyleSheet, SafeAreaView, ScrollView } from 'react-native'
import AppHeader from '@/components/app-header'

export default function PrivacyNoticeScreen() {
    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack hideProfile />
            <ScrollView contentContainerStyle={styles.content}>

                <View style={styles.card}>
                    <Text style={styles.heading}>Privacy Notice</Text>
                    <Text style={styles.body}>
                        Assistant Scheduler collects and uses your name, role, shift assignments, and
                        time-off request information solely to operate the scheduling platform for your
                        dental office. Your data is stored securely on encrypted AWS servers, transmitted
                        over HTTPS, and is only accessible to authorized personnel based on your role
                        (Assistant, HR, or Manager).{'\n\n'}
                        We do not sell, share, or use your information for any purpose outside of
                        scheduling and employee management. Limited schedule information, such as approved
                        time-off dates, may be visible to relevant team members within the platform.{'\n\n'}
                        You have the right to request a copy of the data we hold about you or ask for
                        corrections by contacting your HR administrator. By using Assistant Scheduler, you
                        agree to the collection and use of your information as described in this notice.
                    </Text>
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
        padding: 20,
        shadowColor: '#000',
        shadowOpacity: 0.08,
        shadowRadius: 6,
        elevation: 2,
    },
    heading: {
        fontSize: 22,
        fontWeight: 'bold',
        color: '#1a1a2e',
        marginBottom: 16,
        textAlign: 'center',
    },
    body: {
        fontSize: 15,
        color: '#444',
        lineHeight: 23,
    },
})
