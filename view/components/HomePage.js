import React from 'react';
import Navbar from './Navbar';

const HomePage = ({ user, setUser, setCurrentPage }) => {
  return (
    <div className="homepage">
      <Navbar user={user} setUser={setUser} setCurrentPage={setCurrentPage} />
      <main className="main-content">
        <div className="hero-section">
          <h1 className="hero-title">Welcome to Your Dashboard</h1>
          <p className="hero-subtitle">
            You've successfully logged in! This is your personalized homepage where you can access all your features and content.
          </p>
        </div>

        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon blue">👤</div>
              <h3>Profile Management</h3>
            </div>
            <p>Update your personal information, change your password, and manage your account settings.</p>
            <button className="feature-btn">Manage Profile →</button>
          </div>

          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon green">🏠</div>
              <h3>Dashboard</h3>
            </div>
            <p>View your activity, analytics, and get insights into your usage patterns and statistics.</p>
            <button className="feature-btn">View Dashboard →</button>
          </div>

          <div className="feature-card">
            <div className="feature-header">
              <div className="feature-icon purple">🔐</div>
              <h3>Security</h3>
            </div>
            <p>Manage your security settings, enable two-factor authentication, and review login activity.</p>
            <button className="feature-btn">Security Settings →</button>
          </div>
        </div>

        <div className="account-info">
          <h2>Account Information</h2>
          <div className="info-grid">
            <div className="info-item">
              <label>Username</label>
              <p>{user?.username}</p>
            </div>
            <div className="info-item">
              <label>Email</label>
              <p>{user?.email}</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default HomePage;