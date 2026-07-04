import { useState } from 'react'
import { View, Text, TextInput, TouchableOpacity, StyleSheet, SafeAreaView, ScrollView } from 'react-native'
import AppHeader from '@/components/app-header'

export default function RateAppScreen() {
    const [rating, setRating] = useState(0)
    const [feedback, setFeedback] = useState('')
    const [submitted, setSubmitted] = useState(false)

    function handleSubmit() {
        setSubmitted(true)
    }

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack hideProfile />
            <ScrollView contentContainerStyle={styles.content}>

                <View style={styles.card}>
                    <Text style={styles.question}>Enjoying Assistant Scheduler?</Text>

                    <View style={styles.stars}>
                        {[1, 2, 3, 4, 5].map((star) => (
                            <TouchableOpacity key={star} onPress={() => setRating(star)}>
                                <Text style={[styles.star, star <= rating && styles.starFilled]}>
                                    {star <= rating ? '★' : '☆'}
                                </Text>
                            </TouchableOpacity>
                        ))}
                    </View>

                    <Text style={styles.subheading}>We'd love your feedback!</Text>

                    <TextInput
                        style={styles.feedbackInput}
                        placeholder="Tell us more..."
                        placeholderTextColor="#aaa"
                        value={feedback}
                        onChangeText={setFeedback}
                        multiline
                        numberOfLines={5}
                        editable={!submitted}
                    />

                    {!submitted ? (
                        <TouchableOpacity style={styles.submitBtn} onPress={handleSubmit}>
                            <Text style={styles.submitText}>Submit</Text>
                        </TouchableOpacity>
                    ) : (
                        <Text style={styles.thankYou}>Thank you!</Text>
                    )}
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
        alignItems: 'center',
    },
    card: {
        backgroundColor: 'white',
        borderRadius: 14,
        padding: 24,
        width: '100%',
        shadowColor: '#000',
        shadowOpacity: 0.08,
        shadowRadius: 6,
        elevation: 2,
        alignItems: 'center',
    },
    question: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#1a1a2e',
        textAlign: 'center',
        marginBottom: 20,
    },
    stars: {
        flexDirection: 'row',
        gap: 8,
        marginBottom: 20,
    },
    star: {
        fontSize: 38,
        color: '#ccc',
    },
    starFilled: {
        color: '#f5c518',
    },
    subheading: {
        fontSize: 16,
        color: '#555',
        marginBottom: 16,
        textAlign: 'center',
    },
    feedbackInput: {
        width: '100%',
        backgroundColor: '#f0f0f5',
        borderRadius: 10,
        padding: 12,
        fontSize: 15,
        color: '#333',
        minHeight: 120,
        textAlignVertical: 'top',
        marginBottom: 20,
    },
    submitBtn: {
        backgroundColor: '#443066',
        borderRadius: 10,
        paddingVertical: 14,
        paddingHorizontal: 48,
        alignItems: 'center',
    },
    submitText: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
    },
    thankYou: {
        fontSize: 18,
        color: '#443066',
        fontWeight: 'bold',
        marginTop: 8,
    },
})
