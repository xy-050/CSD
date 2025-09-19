import { useState } from "react";
import SearchBar from "./Searchbar"

export default function Homepage({ user, setUser, setCurrentPage }) {
  const [lastQuery, setLastQuery] = useState("");
  const logout = () => { setUser(null); setCurrentPage('login') };
  const handleSearch = (q) => console.log("Searching:", q);

return (
  <div className="homepage">
    {/* Navbar */}
    <nav className="navbar">
      <div className="nav-container">
        <div className="nav-brand">
          <span className="nav-icon">💵</span>
          <span className="nav-title">Tariff-ic!</span>
        </div>

        <div className="desktop-menu">
          <a className="nav-link" href="#" onClick={(e) => e.preventDefault()}>Dashboard</a>
          <a className="nav-link" href="#" onClick={(e) => e.preventDefault()}>Docs</a>
          <a className="nav-link" href="#" onClick={(e) => e.preventDefault()}>Settings</a>
        </div>

        <div className="nav-user">
          <span className="welcome-text">Hi, <b>{user?.username}</b></span>
          <button className="logout-btn" onClick={logout}>Log out</button>
        </div>
      </div>
    </nav>

    {/* Main */}
    <main className="main-content">
      <section className="hero-section">
        <h1 className="hero-title">Welcome to your dashboard</h1>
        {/* SearchBar */}
        <SearchBar onSearch={handleSearch} />
        {lastQuery && (
          <p className="hero-subtitle" style={{ marginTop: "0.75rem" }}>
            Showing results for: <b>{lastQuery}</b>
          </p>
        )}
      </section>

      <section className="features-grid">
        <article className="feature-card">
          <div className="feature-header">
            <div className="feature-icon blue">📈</div>
            <h3>Insights</h3>
          </div>
          <p>Track activity and recent events at a glance.</p>
          <button className="feature-btn">View reports</button>
        </article>

        <article className="feature-card">
          <div className="feature-header">
            <div className="feature-icon green">🗂️</div>
            <h3>Projects</h3>
          </div>
          <p>Organize work with a soothing, minimal UI.</p>
          <button className="feature-btn">Open projects</button>
        </article>

        <article className="feature-card">
          <div className="feature-header">
            <div className="feature-icon purple">⚙️</div>
            <h3>Settings</h3>
          </div>
          <p>Tune preferences and notification rules.</p>
          <button className="feature-btn">Manage</button>
        </article>
      </section>

      <section className="account-info">
        <h2>Account</h2>
        <div className="info-grid mt-3">
          <div className="info-item">
            <label>Username</label>
            <p>{user?.username}</p>
          </div>
          <div className="info-item">
            <label>Email</label>
            <p>{user?.email}</p>
          </div>
        </div>
      </section>
    </main>
  </div>
)
}