import React, { useState } from 'react';

const Navbar = ({ user, setUser, setCurrentPage }) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    setUser(null);
    setCurrentPage('login');
    setMobileMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <div className="nav-brand">
          <span className="nav-icon">🏠</span>
          <span className="nav-title">MyApp</span>
        </div>

        {/* Desktop menu */}
        <div className="nav-menu desktop-menu">
          <a href="#" className="nav-link">Dashboard</a>
          <a href="#" className="nav-link">Profile</a>
          <a href="#" className="nav-link">Settings</a>
          <div className="nav-user">
            <span className="welcome-text">Welcome, {user?.username}!</span>
            <button onClick={handleLogout} className="logout-btn">
              🚪 Logout
            </button>
          </div>
        </div>

        {/* Mobile menu button */}
        <div className="mobile-menu-btn">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="hamburger"
          >
            {mobileMenuOpen ? '✕' : '☰'}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="mobile-menu">
          <a href="#" className="mobile-nav-link">Dashboard</a>
          <a href="#" className="mobile-nav-link">Profile</a>
          <a href="#" className="mobile-nav-link">Settings</a>
          <div className="mobile-user-section">
            <p className="mobile-welcome">Welcome, {user?.username}!</p>
            <button onClick={handleLogout} className="mobile-logout-btn">
              🚪 Logout
            </button>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;