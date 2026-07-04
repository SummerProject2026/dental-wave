import { useState, useRef } from 'react'
import {
    View, Text, TextInput, TouchableOpacity,
    StyleSheet, SafeAreaView, FlatList, KeyboardAvoidingView, Platform
} from 'react-native'
import AppHeader from '@/components/app-header'

type Message = {
    id: string
    sender: string
    text: string
    isMe: boolean
}

const initialMessages: Message[] = [
    {
        id: '1',
        sender: 'Manager',
        text: 'OOOOooo, ok! Perfect! I think that if what I am going to do then, that\'d be a private page for just edit and add!',
        isMe: false,
    },
    {
        id: '2',
        sender: 'Me',
        text: 'yeah sounds good. also I never thought I would communicate with someone over figma! lol! its almost like communicating in google docs when you are grounded and have your phone taken away lol',
        isMe: true,
    },
    {
        id: '3',
        sender: 'Manager',
        text: 'Hahahaha YES!!!',
        isMe: false,
    },
    {
        id: '4',
        sender: 'Me',
        text: 'I love how we are spending time trying to make sure we have a message board! Haha',
        isMe: true,
    },
    {
        id: '5',
        sender: 'Manager',
        text: 'Ihatuh love that!',
        isMe: false,
    },
    {
        id: '6',
        sender: 'Me',
        text: 'We will just have to have a designated board for communication, and everyone in a while clear it.',
        isMe: true,
    },
    {
        id: '7',
        sender: 'Manager',
        text: 'I love that idea!! sounds fun!',
        isMe: false,
    },
    {
        id: '8',
        sender: 'Me',
        text: 'wonderful! Well I guess I will get back to work...',
        isMe: true,
    },
    {
        id: '9',
        sender: 'Manager',
        text: 'same here! nice talking to you!',
        isMe: false,
    },
    {
        id: '10',
        sender: 'Me',
        text: 'Same!',
        isMe: true,
    },
    {
        id: '11',
        sender: 'Manager',
        text: 'Do you have a moment to jump on discord? I have some more questions?',
        isMe: false,
    },
    {
        id: '12',
        sender: 'Me',
        text: 'sure',
        isMe: true,
    },
]

export default function MessageBoardScreen() {
    const [messages, setMessages] = useState<Message[]>(initialMessages)
    const [input, setInput] = useState('')
    const listRef = useRef<FlatList>(null)

    function sendMessage() {
        const text = input.trim()
        if (!text) return
        setMessages(prev => [
            ...prev,
            {
                id: String(Date.now()),
                sender: 'Me',
                text,
                isMe: true,
            },
        ])
        setInput('')
        setTimeout(() => listRef.current?.scrollToEnd({ animated: true }), 100)
    }

    return (
        <SafeAreaView style={styles.page}>
            <AppHeader showBack />
            <KeyboardAvoidingView
                style={styles.flex}
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                keyboardVerticalOffset={0}
            >
                <FlatList
                    ref={listRef}
                    data={messages}
                    keyExtractor={item => item.id}
                    contentContainerStyle={styles.messageList}
                    onContentSizeChange={() => listRef.current?.scrollToEnd({ animated: false })}
                    renderItem={({ item }) => (
                        <View style={[styles.bubbleRow, item.isMe && styles.bubbleRowMe]}>
                            <View style={[styles.bubble, item.isMe ? styles.bubbleMe : styles.bubbleThem]}>
                                <Text style={[styles.bubbleText, item.isMe && styles.bubbleTextMe]}>
                                    {item.text}
                                </Text>
                            </View>
                        </View>
                    )}
                />
                <View style={styles.inputRow}>
                    <TextInput
                        style={styles.input}
                        value={input}
                        onChangeText={setInput}
                        placeholder="Type a message..."
                        placeholderTextColor="#aaa"
                        onSubmitEditing={sendMessage}
                        returnKeyType="send"
                    />
                    <TouchableOpacity style={styles.sendBtn} onPress={sendMessage}>
                        <Text style={styles.sendIcon}>➤</Text>
                    </TouchableOpacity>
                </View>
            </KeyboardAvoidingView>
        </SafeAreaView>
    )
}

const styles = StyleSheet.create({
    page: {
        flex: 1,
        backgroundColor: '#c8cbff',
    },
    flex: {
        flex: 1,
    },
    messageList: {
        padding: 16,
        gap: 10,
    },
    bubbleRow: {
        flexDirection: 'row',
        justifyContent: 'flex-start',
    },
    bubbleRowMe: {
        justifyContent: 'flex-end',
    },
    bubble: {
        maxWidth: '75%',
        borderRadius: 16,
        paddingHorizontal: 14,
        paddingVertical: 10,
    },
    bubbleThem: {
        backgroundColor: 'white',
        borderBottomLeftRadius: 4,
    },
    bubbleMe: {
        backgroundColor: '#443066',
        borderBottomRightRadius: 4,
    },
    bubbleText: {
        fontSize: 15,
        color: '#1a1a2e',
        lineHeight: 21,
    },
    bubbleTextMe: {
        color: 'white',
    },
    inputRow: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 16,
        paddingVertical: 12,
        backgroundColor: '#c8cbff',
        gap: 10,
    },
    input: {
        flex: 1,
        backgroundColor: 'white',
        borderRadius: 24,
        paddingHorizontal: 16,
        paddingVertical: 12,
        fontSize: 15,
        color: '#333',
    },
    sendBtn: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: '#443066',
        justifyContent: 'center',
        alignItems: 'center',
    },
    sendIcon: {
        color: 'white',
        fontSize: 18,
    },
})
