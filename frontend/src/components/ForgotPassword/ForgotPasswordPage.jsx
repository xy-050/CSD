import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/AxiosConfig.jsx';

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

    const handleEmailSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            await api.post('/forgot-password', { email });
            setSuccess('Reset token sent to your email!');
            setTimeout(() => {
                setStep(2);
                setSuccess('');
            }, 1500);
        } catch (err) {
            console.error(err);
            setError('Something went wrong. Please try again.');
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

        // Validate password length
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
            // Adjust endpoint/payload to match your backend
            await api.post('/reset-password', {
                email,
                token,
                password
            });

            setSuccess('Password reset successful! Redirecting to login...');
            setTimeout(() => {
                navigate('/login');
            }, 2000);
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
                <div className="login-brand">
                    <div className="login-logo-circle">
                        💸
                    </div>
                    <span className="login-brand-name">Tariff-ic</span>
                </div>

                {step === 1 ? (
                    <>
                        <div className="login-header login-header-compact">
                            <h1>Reset password</h1>
                            <p>Enter the email linked to your account.</p>
                        </div>

                        {error && <div className="error-message mb-3">{error}</div>}
                        {success && <div className="success-message mb-3">{success}</div>}

                        <form className="form-container" onSubmit={handleEmailSubmit}>
                            <div className="input-group">
                                <label>Email</label>
                                <div className="input-wrapper">
                                    <span className="input-icon">📧</span>
                                    <input
                                        type="email"
                                        placeholder="you@example.com"
                                        value={email}
                                        onChange={(e) => {
                                            setEmail(e.target.value);
                                            if (error) setError('');
                                        }}
                                        required
                                    />
                                </div>
                            </div>

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

                        {error && <div className="error-message mb-3">{error}</div>}
                        {success && <div className="success-message mb-3">{success}</div>}

                        <form className="form-container" onSubmit={handlePasswordReset}>
                            <div className="input-group">
                                <label>Reset Token</label>
                                <div className="input-wrapper">
                                    <span className="input-icon">🔑</span>
                                    <input
                                        type="text"
                                        placeholder="Enter token from email"
                                        value={token}
                                        onChange={(e) => {
                                            setToken(e.target.value);
                                            if (error) setError('');
                                        }}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>New Password</label>
                                <div className="input-wrapper">
                                    <span className="input-icon">🔒</span>
                                    <input
                                        type="password"
                                        placeholder="Enter new password"
                                        value={password}
                                        onChange={(e) => {
                                            setPassword(e.target.value);
                                            if (error) setError('');
                                        }}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Confirm New Password</label>
                                <div className="input-wrapper">
                                    <span className="input-icon">🔒</span>
                                    <input
                                        type="password"
                                        placeholder="Confirm new password"
                                        value={confirmPassword}
                                        onChange={(e) => {
                                            setConfirmPassword(e.target.value);
                                            if (error) setError('');
                                        }}
                                        required
                                    />
                                </div>
                            </div>

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