import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from "../api/AxiosConfig.jsx";

export default function LoginPage() {
    const navigate = useNavigate()
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await api.post("/login", {
                username: username, 
                password: password
            });

            console.log(response);

            if (response.status === 200 && response.data.token) {
                localStorage.setItem('token', response.data.token);
                navigate("/");
            } else {
                setError("Invalid response from server. PLease try again.");
            }
        } catch (error) {
            setError("Login failed. Please try again.");
            console.log("Error: " + error);
        }

        setLoading(false);
    }

    return (
        <div className="login-container">
            <div className="login-box">
                {/* Brand strip like Mangools */}
                <div className="login-brand">
                    <div className="login-logo-circle">
                        💸
                    </div>
                    <span className="login-brand-name">Tariff-ic</span>
                </div>

                <div className="login-header login-header-compact">
                    <h1>Welcome back!</h1>
                    <p>Sign in to continue</p>
                </div>

                {error && <div className="error-message mb-3">{error}</div>}

                <form className="form-container" onSubmit={handleLogin}>
                    <div className="input-group">
                        <label>Username</label>
                        <div className="input-wrapper">
                            <span className="input-icon">🪪</span>
                            <input
                                placeholder="demo"
                                value={username}
                                onChange={(e) => {
                                    setUsername(e.target.value)
                                    if (error) setError('')
                                }}
                                required
                            />
                        </div>
                    </div>

                    <div className="input-group">
                        <label>Password</label>
                        <div className="input-wrapper">
                            <span className="input-icon">🔒</span>
                            <input
                                type="password"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => {
                                    setPassword(e.target.value)
                                    if (error) setError('')
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
                        {loading ? 'Signing in...' : 'Sign in'}
                    </button>
                </form>

                {/* Bottom inline links like the screenshot */}
                <div className="login-footer-links">
                    <Link to="/signup" className="link-btn">
                        Don&apos;t have an account?
                    </Link>
                    <span className="footer-divider">•</span>
                    <Link to="/forgot-password" className="link-btn">
                        Forgot password?
                    </Link>
                </div>
            </div>
        </div>
    )
}
