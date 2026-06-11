import '../App.css'
import logo from '../pictures/wake-logo.png'
import EmployeeHeader from '../components/EmployeeHeader'

function EmployeeProfilePage() {
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
                            <h3>Your Name</h3>
                            <p>username</p>
                        </div>
                    </div>

                    <div className="profile-form">

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Name:</span>
                                <input readOnly />
                            </div>

                            <div className="profile-row">
                                <span>User Name:</span>
                                <input readOnly />
                            </div>

                            <div className="profile-row">
                                <span>Email:</span>
                                <input readOnly />
                            </div>

                            <div className="profile-row">
                                <span>Phone Number:</span>
                                <input readOnly />
                            </div>

                            <div className="profile-row">
                                <span>Password:</span>
                                <input readOnly type="password" value="password" />
                            </div>
                        </div>

                        <div className="profile-column">
                            <div className="profile-row">
                                <span>Status:</span>
                                <input readOnly />
                            </div>

                            <div className="profile-row">
                                <span>Hire Date:</span>
                                <input readOnly />
                            </div>

                            <div className="profile-row">
                                <span>PTO:</span>
                                <input readOnly />
                            </div>

                            <button className="edit-profile-button">
                                ✏️
                            </button>
                        </div>

                    </div>

                </section>

            </main>

            <footer className="calendar-footer">
                © All Rights Reserved
            </footer>

        </div>
    )
}

export default EmployeeProfilePage