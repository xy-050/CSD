import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import api from "../api/AxiosConfig.jsx";

import { Logo } from '../components/ui/Logo/Logo.jsx';
import { FormInput } from '../components/ui/FormInput/FormInput.jsx';
import { ErrorMessage } from '../components/ui/ErrorMessage/ErrorMessage.jsx';

export default function LoginPage() {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        console.log("Axios baseURL:", isLocalhost ? 'http:localhost:8080' : 'api');
    }, []);

    const clearError = () => error && setError('');

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await api.post("/login", {
                username,
                password
            });

            console.log(response);

            if (response.status === 200 && response.data.token) {
                localStorage.setItem('token', response.data.token);
                navigate("/");
            } else {
                setError("Invalid response from server. Please try again.");
            }
        } catch (error) {
            // More detailed error handling like SignupPage
            const errorMessage = error.response?.data
                || "Login failed. Please try again.";
            setError(errorMessage);
            console.log("Error: " + error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <Logo />

                <div className="login-header login-header-compact">
                    <h1>Welcome back!</h1>
                    <p>Sign in to continue</p>
                </div>

                <ErrorMessage message={error} />

                <form className="form-container" onSubmit={handleLogin}>
                    <FormInput
                        label="Username"
                        icon="🪪"
                        placeholder="demo"
                        value={username}
                        onChange={(e) => {
                            setUsername(e.target.value);
                            clearError();
                        }}
                        required
                    />

                    <FormInput
                        label="Password"
                        icon="🔒"
                        type="password"
                        placeholder="••••••••"
                        value={password}
                        onChange={(e) => {
                            setPassword(e.target.value);
                            clearError();
                        }}
                        required
                    />

                    <button
                        className="submit-btn login-btn"
                        type="submit"
                        disabled={loading}
                    >
                        {loading ? 'Signing in...' : 'Sign in'}
                    </button>
                </form>

                <div className="login-footer-links">
                    <Link to="/signup" className="link-btn">
                        Don't have an account?
                    </Link>
                    <span className="footer-divider">•</span>
                    <Link to="/forgot-password" className="link-btn">
                        Forgot password?
                    </Link>
                </div>
            </div>
        </div>
    );
}
