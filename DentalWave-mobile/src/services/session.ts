/**
 * Lightweight in-memory session store for the mobile app.
 *
 * Holds the authenticated user's token and ids for the lifetime of the
 * running app session. Kept dependency-free (no AsyncStorage) since the
 * assistant app does not currently need to persist login across restarts.
 */

export type Session = {
    accessToken: string
    userId: number
    employeeId: number | null
    username: string
    role: string
}

let currentSession: Session | null = null

/** Stores the session after a successful login. */
export function setSession(session: Session) {
    currentSession = session
}

/** Returns the current session, or null if not logged in. */
export function getSession(): Session | null {
    return currentSession
}

/** Convenience accessor for the logged-in user id. */
export function getUserId(): number | null {
    return currentSession?.userId ?? null
}

/** Clears the session on logout. */
export function clearSession() {
    currentSession = null
}
