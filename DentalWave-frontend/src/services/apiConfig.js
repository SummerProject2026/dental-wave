const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()

export const API_BASE_URL = (
    configuredBaseUrl || (import.meta.env.DEV ? 'http://localhost:8080' : '')
).replace(/\/$/, '')

export function apiUrl(path) {
    const normalizedPath = path.startsWith('/') ? path : `/${path}`
    return `${API_BASE_URL}${normalizedPath}`
}
