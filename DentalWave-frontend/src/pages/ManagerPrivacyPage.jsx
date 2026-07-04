import '../App.css'
import logo from '../pictures/wake-logo.png'
import ManagerHeader from '../components/ManagerHeader'

/**
 * Manager's Privacy Policy page (matches the Figma "Manager's privacy policy page").
 * Static informational content describing how the platform handles data.
 */
function ManagerPrivacyPage() {
    return (
        <div className="profile-page">

            <ManagerHeader />

            <main className="privacy-layout">

                <aside className="privacy-sidebar">
                    <img src={logo} alt="Wake Orthodontics" className="profile-logo" />
                </aside>

                <section className="privacy-content">
                    <h1 className="privacy-title">Privacy and Policy</h1>

                    <p className="privacy-text">
                        Assistant Scheduler collects and uses your name, role, shift assignments,
                        and time-off request information solely to operate the scheduling platform
                        for your dental office. Your data is stored securely on encrypted AWS
                        servers, transmitted over HTTPS, and is only accessible to authorized
                        personnel based on your role (Assistant, HR, or Manager). We do not sell,
                        share, or use your information for any purpose outside of scheduling and
                        employee management. Limited schedule information, such as approved time-off
                        dates, may be visible to colleagues on the shared calendar. Your account
                        data is retained for the duration of your employment and up to three years
                        after deactivation for operational purposes. You have the right to access,
                        correct, or request deletion of your data — contact your HR administrator to
                        do so. By using this app, you agree to this notice.
                    </p>
                </section>

            </main>

            <footer className="page-footer">© All Rights Reserved</footer>

        </div>
    )
}

export default ManagerPrivacyPage
