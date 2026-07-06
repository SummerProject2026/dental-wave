import { useState } from 'react'
import { useRouter } from 'expo-router'
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, Image, KeyboardAvoidingView,
  Platform, ScrollView
} from 'react-native'
import { setSession } from '@/services/session'
import { API_BASE_URL } from '@/services/api'

export default function LoginScreen() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const router = useRouter()

  async function handleLogin() {
    setError('')
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      })
      if (!response.ok) throw new Error('Invalid credentials')
      const data = await response.json()
      setSession({
        accessToken: data.accessToken,
        userId: data.userId,
        employeeId: data.employeeId ?? null,
        username: data.username,
        firstName: data.firstName ?? null,
        lastName: data.lastName ?? null,
        role: data.role,
      })
      router.replace('/calendar')
    } catch (e) {
      setError('Invalid username or password. Please try again.')
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.page}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={styles.content}>

        <Image
          source={require('../../assets/images/wake-logo.png')}
          style={styles.logo}
          resizeMode="contain"
        />

        <Text style={styles.label}>Username:</Text>
        <TextInput
          style={styles.input}
          placeholder="username"
          placeholderTextColor="#7b7b22"
          value={username}
          onChangeText={setUsername}
          autoCapitalize="none"
          autoCorrect={false}
        />

        <Text style={styles.label}>Password:</Text>
        <View style={styles.passwordRow}>
          <TextInput
            style={[styles.input, styles.passwordInput]}
            placeholder="••••••••••••"
            placeholderTextColor="#aaa"
            value={password}
            onChangeText={setPassword}
            secureTextEntry={!showPassword}
          />
          <TouchableOpacity
            style={styles.eyeButton}
            onPress={() => setShowPassword((prev) => !prev)}
          >
            <Text style={styles.eyeIcon}>{showPassword ? '🙈' : '👁️'}</Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity>
          <Text style={styles.forgotLink}>Forgot Password?</Text>
        </TouchableOpacity>

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <TouchableOpacity style={styles.button} onPress={handleLogin}>
          <Text style={styles.buttonText}>Login</Text>
        </TouchableOpacity>

        <Text style={styles.footer}>© All Rights Reserved</Text>

      </ScrollView>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: '#c8cbff',
  },
  content: {
    flexGrow: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
    paddingVertical: 48,
  },
  logo: {
    width: 300,
    height: 160,
    marginBottom: 24,
  },
  label: {
    alignSelf: 'flex-start',
    fontSize: 26,
    fontWeight: 'bold',
    color: '#2444d8',
    marginTop: 16,
    marginBottom: 8,
  },
  input: {
    width: '100%',
    height: 52,
    backgroundColor: 'white',
    borderRadius: 8,
    paddingHorizontal: 14,
    fontSize: 18,
    color: '#333',
  },
  passwordRow: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  passwordInput: {
    flex: 1,
    paddingRight: 48,
  },
  eyeButton: {
    position: 'absolute',
    right: 14,
    height: 52,
    justifyContent: 'center',
  },
  eyeIcon: {
    fontSize: 20,
  },
  forgotLink: {
    alignSelf: 'flex-end',
    fontSize: 14,
    color: '#007e96',
    marginTop: 8,
    marginBottom: 4,
    textDecorationLine: 'underline',
  },
  error: {
    color: 'red',
    fontSize: 14,
    marginTop: 12,
    textAlign: 'center',
  },
  button: {
    marginTop: 28,
    backgroundColor: '#443066',
    borderRadius: 8,
    width: 140,
    height: 54,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
  footer: {
    marginTop: 48,
    fontSize: 14,
    color: '#777',
  },
})
