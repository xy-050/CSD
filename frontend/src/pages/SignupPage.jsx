import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import api from '../api/AxiosConfig.jsx';

import { Logo } from '../components/ui/Logo/Logo.jsx';
import { FormInput } from '../components/ui/FormInput/FormInput.jsx';
import { ErrorMessage } from '../components/ui/ErrorMessage/ErrorMessage.jsx';

export default function SignupPage() {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const navigate = useNavigate();

    const clearError = () => error && setError('');

    const validateForm = () => {
        if (!username.trim()) {
            return 'Username is required.';
        }
        if (!email.trim()) {
            return 'Email is required.';
        }
        if (!password) {
            return 'Password is required.';
        }
        if (password !== confirm) {
            return 'Passwords do not match.';
        }
        if (password.length < 8) {
            return 'Password must be at least 8 characters.';
        }
        return null;
    };

    const handleSignup = async (e) => {
        e.preventDefault();
        setError('');

        // Validate
        const validationError = validateForm();
        if (validationError) {
            setError(validationError);
            return;
        }

        // Submit
        setIsLoading(true);
        try {
            const response = await api.post("/signup", {
                username,
                email,
                password
            });
            console.log(response);
            navigate("/login");
        } catch (error) {
            const errorMessage = error.response?.data
                || "Account creation failed. Please try again.";
            setError(errorMessage);
            console.log("Error: " + error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="signup-container">
            <div className="signup-box">
                <Logo />

                <div className="signup-header login-header-compact">
                    <h1>Create account</h1>
                    <p>Join us on this Tariff-ic day! ✨</p>
                </div>

                <ErrorMessage message={error} />

                <form className="form-container" onSubmit={handleSignup}>
                    <FormInput
                        label="Username"
                        icon="👤"
                        placeholder="yourname"
                        value={username}
                        onChange={(e) => {
                            setUsername(e.target.value);
                            clearError();
                        }}
                        required
                    />

                    <FormInput
                        label="Email"
                        icon="📧"
                        type="email"
                        placeholder="you@example.com"
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            clearError();
                        }}
                        required
                    />

                    <FormInput
                        label="Password"
                        icon="🔒"
                        type="password"
                        placeholder="At least 8 characters"
                        value={password}
                        onChange={(e) => {
                            setPassword(e.target.value);
                            clearError();
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
                            clearError();
                        }}
                        required
                    />

                    <button
                        className="submit-btn signup-btn"
                        type="submit"
                        disabled={isLoading}
                    >
                        {isLoading ? 'SIGNING UP...' : 'SIGN UP'}
                    </button>
                </form>

                <div className="switch-form">
                    <p>
                        Already have an account?{' '}
                        <Link to="/login" className="link-btn">
                            Log in
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
