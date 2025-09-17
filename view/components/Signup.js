import React, { useState } from 'react';

const SignupPage = ({ setCurrentPage, setUser, users, setUsers }) => {
  const [formData, setFormData] = useState({ 
    username: '', 
    email: '', 
    password: '', 
    confirmPassword: '' 
  });
  const [errors, setErrors] = useState({});

  const handleSubmit = () => {
    const newErrors = {};

    if (!formData.username) newErrors.username = 'Username is required';
    if (!formData.email) newErrors.email = 'Email is required';
    if (!formData.password) newErrors.password = 'Password is required';
    if (!formData.confirmPassword) newErrors.confirmPassword = 'Please confirm your password';

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    if (formData.password && formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    const existingUser = users.find(u => u.username === formData.username || u.email === formData.email);
    if (existingUser) {
      newErrors.general = 'Username or email already exists';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const newUser = {
      username: formData.username,
      email: formData.email,
      password: formData.password
    };

    setUsers([...users, newUser]);
    setUser(newUser);
    setCurrentPage('home');
    setErrors({});
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-box">
        <div className="signup-header">
          <h1>Create Account</h1>
          <p>Join us today</p>
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
                placeholder="Choose a username"
              />
            </div>
            {errors.username && <p className="field-error">{errors.username}</p>}
          </div>

          <div className="input-group">
            <label>Email</label>
            <div className="input-wrapper">
              <span className="input-icon">✉️</span>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
                onKeyPress={handleKeyPress}
                className={errors.email ? 'error' : ''}
                placeholder="Enter your email"
              />
            </div>
            {errors.email && <p className="field-error">{errors.email}</p>}
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
                placeholder="Create a password"
              />
            </div>
            {errors.password && <p className="field-error">{errors.password}</p>}
          </div>

          <div className="input-group">
            <label>Confirm Password</label>
            <div className="input-wrapper">
              <span className="input-icon">🔒</span>
              <input
                type="password"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})}
                onKeyPress={handleKeyPress}
                className={errors.confirmPassword ? 'error' : ''}
                placeholder="Confirm your password"
              />
            </div>
            {errors.confirmPassword && <p className="field-error">{errors.confirmPassword}</p>}
          </div>

          <button onClick={handleSubmit} className="submit-btn signup-btn">
            Create Account
          </button>
        </div>

        <div className="switch-form">
          <p>
            Already have an account?{' '}
            <button onClick={() => setCurrentPage('login')} className="link-btn">
              Sign in
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignupPage;