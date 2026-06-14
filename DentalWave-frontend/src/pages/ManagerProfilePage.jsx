import '../App.css'
import logo from '../pictures/wake-logo.png'
import ManagerHeader from '../components/ManagerHeader'
import { useState } from 'react'

function ManagerProfilePage() {

    const [isEditing, setIsEditing] = useState(false)
    const [form, setForm] = useState({
        name: '',
        username: '',
        email: '',
        phone: '',
        password: '',
        repeatPassword: '',
        status: '',
        hireDate: '',
        pto: ''
    })

    function handleChange(e) {
        setForm({ ...form, [e.target.name]: e.target.value })
    }

    function handleSave() {
        setIsEditing(false)
        // TODO: call API to save changes
    }

    return (
        <div className="profile-page">

            <ManagerHeader />

            <main className="profile-layout">

                <aside className="profile-sidebar">
                    <img
                        src={logo}
                        alt="Wake Orthodontics"
                        className="profile-logo"
                    />
                </aside>

                <section className="profile-content">

                    <div className="profile-top">
                        <div className="tooth-icon">🦷</div>
                        <div>
                            <h3>{form.name || 'Your Name'}</h3>
                            <p>{form.username || 'username'}</p>
                        </div>
                    </div>

                    <div className="profile-form">

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Name:</span>
                                <input
                                    name="name"
                                    value={form.name}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>User Name:</span>
                                <input
                                    name="username"
                                    value={form.username}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Email:</span>
                                <input
                                    name="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Phone number:</span>
                                <input
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Password:</span>
                                <input
                                    name="password"
                                    type="password"
                                    value={form.password}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            {isEditing && (
                                <div className="profile-row">
                                    <span>Repeat Password:</span>
                                    <input
                                        name="repeatPassword"
                                        type="password"
                                        value={form.repeatPassword}
                                        onChange={handleChange}
                                    />
                                </div>
                            )}
                        </div>

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Status:</span>
                                <input
                                    name="status"
                                    value={form.status}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>Hire Date:</span>
                                <input
                                    name="hireDate"
                                    value={form.hireDate}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>
                            <div className="profile-row">
                                <span>PTO:</span>
                                <input
                                    name="pto"
                                    value={form.pto}
                                    onChange={handleChange}
                                    readOnly={!isEditing}
                                />
                            </div>

                            {isEditing ? (
                                <button
                                    className="save-profile-button"
                                    onClick={handleSave}
                                >
                                    Save
                                </button>
                            ) : (
                                <button
                                    className="edit-profile-button"
                                    onClick={() => setIsEditing(true)}
                                >
                                    ✏️
                                </button>
                            )}
                        </div>

                    </div>

                </section>

            </main>

            <footer className="page-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default ManagerProfilePage