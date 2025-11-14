import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

import api from "../api/AxiosConfig.jsx";

import NavBar from "../components/layout/NavBar/NavBar.jsx";
import Sidebar from "../components/layout/SideBar/Sidebar.jsx";
import { FormInput } from "../components/ui/FormInput/FormInput.jsx";
import { ErrorMessage } from "../components/ui/ErrorMessage/ErrorMessage.jsx";
import { SuccessMessage } from "../components/ui/SuccessMessage/SuccessMessage.jsx";
import Popup from "../components/ui/Popup/Popup.jsx";

import { useCurrentUser } from "../hooks/auth/useCurrentUser.jsx";
import { useResponsiveSidebar } from '../hooks/ui/useResponsiveSidebar.jsx';

export default function ProfilePage() {
    const { user, loading, error: fetchError } = useCurrentUser();
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [showPopup, setShowPopup] = useState(false);
    const navigate = useNavigate();

    // Sidebar state
    const {
            isOpen: sidebarOpen,
            toggle: toggleSidebar,
            closeOnMobile: closeSidebar
        } = useResponsiveSidebar();

    // Update local state when user data loads
    useEffect(() => {
        if (user.username) {
            setUsername(user.username);
            setEmail(user.email);
        }
    }, [user]);

    // Display fetch error if exists
    useEffect(() => {
        if (fetchError) {
            setError(fetchError);
        }
    }, [fetchError]);

    const handleUpdate = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        try {
            const response = await api.put(`/accounts/${encodeURIComponent(user.userId)}`, {
                userID: user.userId,
                username: username,
                email: email
            });
            console.log(response);

            setSuccess("✅ Profile updated successfully! Redirecting to login...");
            await new Promise(resolve => setTimeout(resolve, 2000));
            localStorage.removeItem("token");
            navigate("/login");
        } catch (error) {
            console.log("Error: " + error.response?.data);

            const errorData = error.response?.data;
            if (errorData?.trim() === "Nothing to update.") {
                setError("❌ Nothing to update. Redirecting to home...");
                await new Promise(resolve => setTimeout(resolve, 2000));
                navigate("/");
                return;
            }

            const errorMessage = error.response?.data?.message
                || error.response?.data
                || "Failed to update profile.";
            setError(errorMessage);
        }
    };

    const handleCancel = () => {
        navigate("/");
    };

    const handleChangePassword = () => {
        navigate("/change_password");
    };

    const handleDelete = () => {
        setShowPopup(true);
    };

    const handleConfirmDelete = async (e) => {
        e.preventDefault();
        setShowPopup(false);
        setError("");

        try {
            const response = await api.delete(`/accounts/${encodeURIComponent(user.userId)}`);
            console.log(response);
            localStorage.removeItem("token");
            navigate("/login");
        } catch (error) {
            console.log("Error: " + error);
            const errorMessage = error.response?.data?.message
                || error.response?.data
                || "Error deleting account. Please try again.";
            setError(errorMessage);
        }
    };

    const handleCancelDelete = () => {
        setShowPopup(false);
    };

    // Show loading state
    if (loading) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content">
                        <div className="loading">Loading profile...</div>
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
                        <h1 className="hero-title">Your Profile</h1>
                        <p className="hero-subtitle">Update your account details.</p>
                    </section>

                    <section className="account-info" id="account">
                        <h2>Account</h2>

                        <ErrorMessage message={error} />
                        <SuccessMessage message={success} />

                        <form className="form-container mt-3" onSubmit={handleUpdate}>
                            <FormInput
                                label="Username"
                                icon="👤"
                                type="text"
                                placeholder="yourname"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />

                            <FormInput
                                label="Email"
                                icon="📧"
                                type="email"
                                placeholder="name@example.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
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
                                <button
                                    type="button"
                                    className="feature-btn secondary"
                                    onClick={handleChangePassword}
                                >
                                    Change Password
                                </button>
                                <button
                                    type="button"
                                    className="feature-btn danger"
                                    onClick={handleDelete}
                                >
                                    Delete Account
                                </button>
                            </div>
                        </form>
                    </section>

                    {showPopup && (
                        <Popup onClose={handleCancelDelete}>
                            <h2>Confirmation</h2>
                            <p>This action is irreversible. Do you want to continue?</p>
                            <div className="button-group">
                                <button
                                    type="button"
                                    className="feature-btn danger"
                                    onClick={handleConfirmDelete}
                                >
                                    Yes, Delete
                                </button>
                                <button
                                    type="button"
                                    className="feature-btn secondary"
                                    onClick={handleCancelDelete}
                                >
                                    Cancel
                                </button>
                            </div>
                        </Popup>
                    )}
                </main>
            </div>
        </div>
    );
}
