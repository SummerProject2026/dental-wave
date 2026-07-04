import { getSession } from './session'

/**
 * Base URL for the DentalWave backend.
 *
 * 10.0.2.2 is the special host the Android emulator uses to reach the
 * host machine's localhost. For the iOS simulator or a physical device,
 * change this to the machine's LAN IP (e.g. http://192.168.x.x:8080).
 */
export const API_BASE_URL = 'http://10.0.2.2:8080'

/** Builds the Authorization header from the current session, if any. */
export function authHeaders(): Record<string, string> {
    const session = getSession()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (session?.accessToken) {
        headers.Authorization = `Bearer ${session.accessToken}`
    }
    return headers
}
