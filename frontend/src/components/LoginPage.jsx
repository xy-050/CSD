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
            const params = new URLSearchParams();
            params.append("username", username);
            params.append("password", password);

            const response = await api.post("/login", params, {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            console.log(response);

            if (response.status == 200) {
                navigate("/");
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
                <div className="login-header">
                    <h1>Welcome back</h1>
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

                <div className="switch-form">
                    <p>
                        New here?{' '}
                        <Link to="/signup" className="link-btn">
                            Create an account
                        </Link>
                    </p>
                </div>

                <div className="demo-info">
                    <p>Demo: <b>demo</b> / <b>password123</b></p>
                </div>
            </div>
        </div>
    )
}
