import '../App.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import logo from '../pictures/wake-logo.png'
import EmployeeHeader from '../components/EmployeeHeader'

function EmployeeEditProfilePage() {
    const navigate = useNavigate()

    const [formData, setFormData] = useState({
        name: '',
        username: '',
        email: '',
        phoneNumber: '',
        password: '',
        repeatPassword: '',
        status: '',
        hireDate: '',
        pto: ''
    })

    function handleChange(event) {
        const { name, value } = event.target

        setFormData({
            ...formData,
            [name]: value
        })
    }

    function handleSubmit(event) {
        event.preventDefault()

        console.log('Updated profile:', formData)

        navigate('/employee/profile')
    }

    return (
        <div className="profile-page">

            <EmployeeHeader />

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
                            <h3>{formData.name || 'Your Name'}</h3>
                            <p>{formData.username || 'username'}</p>
                        </div>
                    </div>

                    <form className="profile-form" onSubmit={handleSubmit}>

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Name:</span>
                                <input
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>User Name:</span>
                                <input
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Email:</span>
                                <input
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Phone Number:</span>
                                <input
                                    name="phoneNumber"
                                    value={formData.phoneNumber}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Password:</span>
                                <input
                                    name="password"
                                    type="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="profile-row">
                                <span>Repeat Password:</span>
                                <input
                                    name="repeatPassword"
                                    type="password"
                                    value={formData.repeatPassword}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Status:</span>
                                <input readOnly value={formData.status} />
                            </div>

                            <div className="profile-row">
                                <span>Hire Date:</span>
                                <input readOnly value={formData.hireDate} />
                            </div>

                            <div className="profile-row">
                                <span>PTO:</span>
                                <input readOnly value={formData.pto} />
                            </div>

                            <button type="submit" className="save-profile-button">
                                Save
                            </button>
                        </div>

                    </form>

                </section>

            </main>

            <footer className="calendar-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default EmployeeEditProfilePage