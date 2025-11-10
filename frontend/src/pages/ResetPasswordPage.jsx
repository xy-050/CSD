import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import api from '../api/AxiosConfig.jsx';

import { Logo } from '../components/ui/Logo/Logo.jsx';
import { FormInput } from '../components/ui/FormInput/FormInput.jsx';
import { ErrorMessage } from '../components/ui/ErrorMessage/ErrorMessage.jsx';
import { SuccessMessage } from '../components/ui/SuccessMessage/SuccessMessage.jsx';

import { useCurrentUser } from '../hooks/auth/useCurrentUser.jsx';

export default function ForgotPasswordPage() {
    const [token, setToken] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isAuth, setIsAuth] = useState(false);
    const [confirm, setConfirm] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const clearMessages = () => {
        if (error) setError('');
        if (success) setSuccess('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            await api.get('/verify-token', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setIsAuth(true);
        } catch (err) {
            console.error(err);
            const errorMessage = err.response?.data?.message
                || err.response?.data
                || 'Something went wrong. Please try again.';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    const validatePasswords = () => {
        if (password !== confirm) {
            return "Passwords do not match.";
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters.";
        }
        if (password === confirm) {
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
            await api.get(`/resetPassword/${email}/${password}`);
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

    return (
        <div className="login-container">
            <div className="login-box">
                <Logo />

                <div className="login-header login-header-compact">
                    <h1>Reset password</h1>
                </div>

                <ErrorMessage message={error} />
                <SuccessMessage message={success} />

                {!isAuth &&
                    <form className="form-container" onSubmit={handleSubmit}>
                        <FormInput
                            label="Token"
                            icon="🪙"
                            value={token}
                            onChange={(e) => {
                                setToken(e.target.value);
                                clearMessages();
                            }}
                            required
                        />

                        <button
                            className="submit-btn login-btn"
                            type="submit"
                            disabled={loading}
                        >
                            {loading ? 'Sending link...' : 'Send reset link'}
                        </button>
                    </form>
                }


                {isAuth &&
                    <form className="form-container" onSubmit={handlePasswordChange}>
                        <FormInput
                            label="Password"
                            icon="🔒"
                            type="password"
                            placeholder="At least 8 characters"
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                clearMessages();
                            }}
                            required
                            minLength={8}
                        />

                        <FormInput
                            label="Confirm password"
                            icon="✅"
                            type="password"
                            placeholder="Repeat password"
                            value={confirm}
                            onChange={(e) => {
                                setConfirm(e.target.value);
                                clearMessages();
                            }}
                            required
                        />

                        <button
                            className="submit-btn login-btn"
                            type="submit"
                            disabled={loading}
                        >
                            Change Password
                        </button>
                    </form>
                }

                <div className="switch-form">
                    <p>
                        Remembered your password?{' '}
                        <Link to="/login" className="link-btn">
                            Go back to login
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
