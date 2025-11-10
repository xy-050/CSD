import { useState } from 'react';
import { Link } from 'react-router-dom';

import api from '../api/AxiosConfig.jsx';

import { Logo } from '../components/ui/Logo/Logo.jsx';
import { FormInput } from '../components/ui/FormInput/FormInput.jsx';
import { ErrorMessage } from '../components/ui/ErrorMessage/ErrorMessage.jsx';
import { SuccessMessage } from '../components/ui/SuccessMessage/SuccessMessage.jsx';

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

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
            await api.post('/forgot-password', { email });
            setSuccess(
                "We've sent a reset link."
            );
            // Optional: Clear the email field after success
            // setEmail('');
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

    return (
        <div className="login-container">
            <div className="login-box">
                <Logo />

                <div className="login-header login-header-compact">
                    <h1>Reset password</h1>
                    <p>Enter the email linked to your account.</p>
                </div>

                <ErrorMessage message={error} />
                <SuccessMessage message={success} />

                <form className="form-container" onSubmit={handleSubmit}>
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
