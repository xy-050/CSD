import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";

import api from '../api/AxiosConfig.jsx';

import NavBar from "../components/layout/NavBar/NavBar.jsx";
import Sidebar from "../components/layout/SideBar/Sidebar.jsx";
import { FormInput } from "../components/ui/FormInput/FormInput.jsx";
import { ErrorMessage } from "../components/ui/ErrorMessage/ErrorMessage.jsx";
import { SuccessMessage } from "../components/ui/SuccessMessage/SuccessMessage.jsx";

import { useCurrentUser } from "../hooks/auth/useCurrentUser.jsx";

export default function ChangePasswordPage() {
    const navigate = useNavigate();

    // Use the custom hook
    const { user, loading, error: fetchError } = useCurrentUser();

    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // Sidebar state
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const toggleSidebar = () => setSidebarOpen(v => !v);
    const closeSidebar = () => setSidebarOpen(false);

    const clearMessages = () => {
        if (error) setError("");
        if (success) setSuccess("");
    };

    // Display fetch error if exists
    useEffect(() => {
        if (fetchError) {
            setError(fetchError);
        }
    }, [fetchError]);

    const validatePasswords = () => {
        if (newPassword !== confirmPassword) {
            return "Passwords do not match.";
        }
        if (newPassword.length < 8) {
            return "Password must be at least 8 characters.";
        }
        if (oldPassword === newPassword) {
            return "New password must be different from old password.";
        }
        return null;
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        // Validate passwords
        const validationError = validatePasswords();
        if (validationError) {
            setError(validationError);
            return;
        }

        try {
            const response = await api.post(`/updatePassword/${encodeURIComponent(user.userId)}`, {
                oldPassword: oldPassword,
                newPassword: newPassword
            });
            console.log(response);

            setSuccess("✅ Password successfully updated! Logging you out...");
            await new Promise(resolve => setTimeout(resolve, 2000));
            localStorage.removeItem("token");
            navigate("/login");
        } catch (error) {
            console.log(error);
            const errorMessage = error.response?.data?.message
                || error.response?.data
                || "Failed to update password. Please try again.";
            setError(errorMessage);
        }
    };

    const handleCancel = () => {
        navigate("/profile");
    };

    // Show loading state
    if (loading) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content">
                        <div className="loading">Loading...</div>
                    </main>
                </div>
            </div>
        );
    }

    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />

                <main className="main-content" onClick={closeSidebar}>
                    <section className="hero-section">
                        <h1 className="hero-title">Change Password</h1>
                        <p className="hero-subtitle">Update your password to keep your account secure.</p>
                    </section>

                    <section className="account-info" id="change-password">
                        <h2>Password Settings</h2>

                        <ErrorMessage message={error} />
                        <SuccessMessage message={success} />

                        <form className="form-container mt-3" onSubmit={handlePasswordChange}>
                            <FormInput
                                label="Old Password"
                                icon="🔒"
                                type="password"
                                placeholder="Enter current password"
                                value={oldPassword}
                                onChange={(e) => {
                                    setOldPassword(e.target.value);
                                    clearMessages();
                                }}
                                required
                                minLength={8}
                            />

                            <FormInput
                                label="New Password"
                                icon="🔐"
                                type="password"
                                placeholder="At least 8 characters"
                                value={newPassword}
                                onChange={(e) => {
                                    setNewPassword(e.target.value);
                                    clearMessages();
                                }}
                                required
                                minLength={8}
                            />

                            <FormInput
                                label="Confirm New Password"
                                icon="✅"
                                type="password"
                                placeholder="Repeat new password"
                                value={confirmPassword}
                                onChange={(e) => {
                                    setConfirmPassword(e.target.value);
                                    clearMessages();
                                }}
                                required
                                minLength={8}
                            />

                            <div className="button-group">
                                <button type="submit" className="feature-btn">
                                    Save changes
                                </button>
                                <button
                                    type="button"
                                    className="feature-btn secondary"
                                    onClick={handleCancel}
                                >
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </section>
                </main>
            </div>
        </div>
    );
}
