import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import api from '../api/AxiosConfig.jsx';

import { Logo } from '../components/ui/Logo/Logo.jsx';
import { FormInput } from '../components/ui/FormInput/FormInput.jsx';
import { ErrorMessage } from '../components/ui/ErrorMessage/ErrorMessage.jsx';
import { SuccessMessage } from '../components/ui/SuccessMessage/SuccessMessage.jsx';

export default function ForgotPasswordPage() {
    const navigate = useNavigate();
    const [step, setStep] = useState(1); // 1: email, 2: token + password
    const [email, setEmail] = useState('');
    const [token, setToken] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const clearMessages = () => {
        if (error) setError('');
        if (success) setSuccess('');
    };

    const handleEmailSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            await api.post('/forgot-password', { email });
            setSuccess("If an account exists for this email, we've sent a reset link.");
            // Move to token/password step after a short delay (optional)
            setTimeout(() => setStep(2), 1200);
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

    const handlePasswordReset = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        // Validate passwords match
        if (password !== confirmPassword) {
            setError('Passwords do not match.');
            setLoading(false);
            return;
        }

        // Validate password length and complexity
        if (password.length < 8) {
            setError('Password must be at least 8 characters long.');
            setLoading(false);
            return;
        }

        const hasUpper = /[A-Z]/.test(password);
        const hasLower = /[a-z]/.test(password);
        const hasDigit = /\d/.test(password);
        const hasSpecial = /[!@#$%^&*()\-+]/.test(password);

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            setError('Password must contain uppercase, lowercase, digit, and special character (!@#$%^&*()-+)');
            setLoading(false);
            return;
        }

        try {
            await api.post('/reset-password', {
                email,
                token,
                password
            });

            setSuccess('Password reset successful! Redirecting to login...');
            setTimeout(() => navigate('/login'), 1500);
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Invalid token or request failed.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <Logo />

                {step === 1 ? (
                    <>
                        <div className="login-header login-header-compact">
                            <h1>Reset password</h1>
                            <p>Enter the email linked to your account.</p>
                        </div>

                        <ErrorMessage message={error} />
                        <SuccessMessage message={success} />

                        <form className="form-container" onSubmit={handleEmailSubmit}>
                            <FormInput
                                label="Email"
                                icon="📧"
                                type="email"
                                placeholder="you@example.com"
                                value={email}
                                onChange={(e) => {
                                    setEmail(e.target.value);
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
                    </>
                ) : (
                    <>
                        <div className="login-header login-header-compact">
                            <h1>Enter reset token</h1>
                            <p>Check your email for the reset token and create a new password.</p>
                        </div>

                        <ErrorMessage message={error} />
                        <SuccessMessage message={success} />

                        <form className="form-container" onSubmit={handlePasswordReset}>
                            <FormInput
                                label="Reset Token"
                                icon="🔑"
                                type="text"
                                placeholder="Enter token from email"
                                value={token}
                                onChange={(e) => {
                                    setToken(e.target.value);
                                    clearMessages();
                                }}
                                required
                            />

                            <FormInput
                                label="New Password"
                                icon="🔒"
                                type="password"
                                placeholder="Enter new password"
                                value={password}
                                onChange={(e) => {
                                    setPassword(e.target.value);
                                    clearMessages();
                                }}
                                required
                            />

                            <FormInput
                                label="Confirm New Password"
                                icon="🔒"
                                type="password"
                                placeholder="Confirm new password"
                                value={confirmPassword}
                                onChange={(e) => {
                                    setConfirmPassword(e.target.value);
                                    clearMessages();
                                }}
                                required
                            />

                            <button
                                className="submit-btn login-btn"
                                type="submit"
                                disabled={loading}
                            >
                                {loading ? 'Resetting password...' : 'Reset password'}
                            </button>
                        </form>

                        <div className="switch-form">
                            <p>
                                Didn't receive the token?{' '}
                                <button
                                    onClick={() => {
                                        setStep(1);
                                        setToken('');
                                        setPassword('');
                                        setConfirmPassword('');
                                        setError('');
                                        setSuccess('');
                                    }}
                                    className="link-btn"
                                    style={{ background: 'none', border: 'none', cursor: 'pointer' }}
                                >
                                    Resend email
                                </button>
                            </p>
                        </div>
                    </>
                )}

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