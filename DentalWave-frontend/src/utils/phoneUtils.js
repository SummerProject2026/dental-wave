export function digitsOnly(value) {
    return String(value || '').replace(/\D/g, '')
}

export function formatPhoneNumber(value) {
    const digits = digitsOnly(value).slice(0, 10)

    if (digits.length <= 3) {
        return digits
    }

    if (digits.length <= 6) {
        return `(${digits.slice(0, 3)}) ${digits.slice(3)}`
    }

    return `(${digits.slice(0, 3)}) ${digits.slice(3, 6)}-${digits.slice(6)}`
}
