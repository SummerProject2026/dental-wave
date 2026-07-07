import { Link } from 'react-router-dom'
import { getLoggedInUser, getLoggedInUserFirstName, getLoggedInUserLastName } from '../services/AuthService'

/** Profile icon showing the logged-in user's initials. */
function UserAvatar({ to }) {
    const firstName = getLoggedInUserFirstName()
    const lastName = getLoggedInUserLastName()
    const username = getLoggedInUser()

    const initials = firstName && lastName
        ? `${firstName[0]}${lastName[0]}`.toUpperCase()
        : (username?.[0] ?? '?').toUpperCase()

    return (
        <Link to={to} className="user-icon avatar-icon">
            <span className="avatar-circle">{initials}</span>
        </Link>
    )
}

export default UserAvatar
