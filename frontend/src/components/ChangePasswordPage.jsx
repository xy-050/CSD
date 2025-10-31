import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import NavBar from "./NavBar";
import api from '../api/AxiosConfig.jsx';

export default function ChangePasswordPage({ }) {
    const navigate = useNavigate();

    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [msg, setMsg] = useState("");
    const [error, setError] = useState("");
    const [userID, setUserID] = useState("");

    useEffect(() => {
        const getUserDetails = async () => {
            try {
                const response = await api.get("/currentUserDetails");
                console.log(response);
                setUserID(response.data.userId);
            } catch (error) {
                console.log(error);
            }
        }
        getUserDetails();
    }, []);

    const handlePasswordChange = async (e) => {
        e.preventDefault();

        setError("");
        if (newPassword != confirmPassword) {
            setError("Password does not match.");
            return;
        }

        try {
            const response = await api.post(`/updatePassword/${encodeURIComponent(userID)}`, {
                oldPassword: oldPassword,
                newPassword: newPassword
            });
            console.log(response);
            setMsg("Password successfuly updated! Logging you out...");
            await new Promise(resolve => setTimeout(resolve, 2000));
            navigate("/login");
        } catch (error) {
            console.log(error);
            setError(error.response.data);
        }
    }

    const handleCancel = () => {
        navigate("/profile");
    }

    return (
        <div className="homepage">
            <NavBar />

            <main className="main-content">
                <section className="hero-section">
                    <h1 className="hero-title">Your profile</h1>
                    <p className="hero-subtitle">Update your account details.</p>
                </section>

                <section className="change-password" id="change-password">
                    <h2>Change Password</h2>

                    {error && (
                        <div className="error-message mb-3" style={{
                            backgroundColor: '#fee',
                            color: '#c33',
                            padding: '12px',
                            borderRadius: '8px',
                            border: '1px solid #fcc',
                            marginBottom: '16px'
                        }}>
                            {error}
                        </div>
                    )}

                    <form className="form-container mt-3" onSubmit={handlePasswordChange}>
                        <div className="input-group">
                            <label>Old Password</label>
                            <div className="input-wrapper">
                                <span className="input-icon">🔒</span>
                                <input
                                    type="password"
                                    placeholder="At least 8 characters"
                                    onChange={(e) => {
                                        setOldPassword(e.target.value)
                                    }}
                                    required
                                    minLength={8}
                                />
                            </div>
                        </div>
                        <div className="input-group">
                            <label>New Password</label>
                            <div className="input-wrapper">
                                <span className="input-icon">🔒</span>
                                <input
                                    type="password"
                                    placeholder="At least 8 characters"
                                    onChange={(e) => {
                                        setNewPassword(e.target.value)
                                    }}
                                    required
                                    minLength={8}
                                />
                            </div>
                        </div>
                        <div className="input-group">
                            <label>Confirm New Password</label>
                            <div className="input-wrapper">
                                <span className="input-icon">🔒</span>
                                <input
                                    type="password"
                                    placeholder="At least 8 characters"
                                    onChange={(e) => {
                                        setConfirmPassword(e.target.value)
                                    }}
                                    required
                                    minLength={8}
                                />
                            </div>
                        </div>

                        {msg && <p className="mt-2" style={{ color: "var(--brand-ink)" }}>{msg}</p>}

                        <div style={{ display: "flex", gap: "0.75rem", marginTop: "0.5rem" }}>
                            <button
                                type="submit"
                                className="feature-btn"
                            >
                                Save changes
                            </button>
                            <button
                                type="button"
                                className="feature-btn"
                                style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
                                onClick={handleCancel}
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </section>
            </main>
        </div>
    )
}