import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/AxiosConfig.jsx';

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            // adjust endpoint / payload to whatever your backend expects
            await api.post('/forgot-password', { email });

            setSuccess(
                'If an account exists for this email, we’ve sent a reset link.'
            );
        } catch (err) {
            console.error(err);
            setError('Something went wrong. Please try again.');
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
                <div className="login-header login-header-compact">
                    <h1>Reset password</h1>
                    <p>Enter the email linked to your account.</p>
                </div>

                {error && <div className="error-message mb-3">{error}</div>}
                {success && <div className="success-message mb-3">{success}</div>}

                <form className="form-container" onSubmit={handleSubmit}>
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
                                    if (success) setSuccess('');
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
