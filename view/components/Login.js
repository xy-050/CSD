import React, { useState } from 'react';

const LoginPage = ({ setCurrentPage, setUser, users }) => {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [errors, setErrors] = useState({});

  const handleSubmit = () => {
    const newErrors = {};

    if (!formData.username) newErrors.username = 'Username is required';
    if (!formData.password) newErrors.password = 'Password is required';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const foundUser = users.find(
      u => u.username === formData.username && u.password === formData.password
    );

    if (foundUser) {
      setUser(foundUser);
      setCurrentPage('home');
      setErrors({});
    } else {
      setErrors({ general: 'Invalid username or password' });
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>Welcome Back</h1>
          <p>Sign in to your account</p>
        </div>

        <div className="form-container">
          {errors.general && (
            <div className="error-message">
              {errors.general}
            </div>
          )}

          <div className="input-group">
            <label>Username</label>
            <div className="input-wrapper">
              <span className="input-icon">👤</span>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({...formData, username: e.target.value})}
                onKeyPress={handleKeyPress}
                className={errors.username ? 'error' : ''}
                placeholder="Enter your username"
              />
            </div>
            {errors.username && <p className="field-error">{errors.username}</p>}
          </div>

          <div className="input-group">
            <label>Password</label>
            <div className="input-wrapper">
              <span className="input-icon">🔒</span>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({...formData, password: e.target.value})}
                onKeyPress={handleKeyPress}
                className={errors.password ? 'error' : ''}
                placeholder="Enter your password"
              />
            </div>
            {errors.password && <p className="field-error">{errors.password}</p>}
          </div>

          <button onClick={handleSubmit} className="submit-btn login-btn">
            Sign In
          </button>
        </div>

        <div className="switch-form">
          <p>
            Don't have an account?{' '}
            <button onClick={() => setCurrentPage('signup')} className="link-btn">
              Sign up
            </button>
          </p>
        </div>

        <div className="demo-info">
          <p>Demo credentials: username: <strong>demo</strong>, password: <strong>password123</strong></p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;