import { useState, useRef, useEffect } from "react";
import SearchBar from "./Searchbar"

export default function Homepage({ user, setUser, setCurrentPage }) {
  const [lastQuery, setLastQuery] = useState("");
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)
  const btnRef = useRef(null)
  const [imgOk, setImgOk] = useState(true);
  const logout = () => { setUser(null); setCurrentPage('login') };
  const handleSearch = (q) => console.log("Searching:", q);

  useEffect(() => {
    const onDown = (e) => {
      if (!menuRef.current || !btnRef.current) return
      if (!menuRef.current.contains(e.target) && !btnRef.current.contains(e.target)) setMenuOpen(false)
    }
    const onKey = (e) => { if (e.key === "Escape") setMenuOpen(false) }
    document.addEventListener("mousedown", onDown)
    document.addEventListener("keydown", onKey)
    return () => { document.removeEventListener("mousedown", onDown); document.removeEventListener("keydown", onKey) }
  }, [])

  const initials = (user?.username || user?.email || "U").split(/\s+/).map(s => s[0]).slice(0, 2).join("").toUpperCase()

  return (
    <div className="homepage">
      {/* Navbar */}
      <nav className="navbar">
        <div className="nav-container">
          <div className="nav-brand">
            <span className="nav-icon">💵</span>
            <span className="nav-title">Tariff-ic!</span>
          </div>
{/* 
          <div className="desktop-menu">
            <a className="nav-link" href="#" onClick={(e) => e.preventDefault()}>Dashboard</a>
            <a className="nav-link" href="#" onClick={(e) => e.preventDefault()}>Docs</a>
            <a className="nav-link" href="#" onClick={(e) => e.preventDefault()}>Settings</a>
          </div> */}

          {/* Avatar + dropdown (top-right) */}
          <div className="nav-user" style={{ position: "relative" }}>
            <button
              ref={btnRef}
              className="avatar-btn"
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              title={user?.username || user?.email}
              onClick={() => setMenuOpen(v => !v)}
            >
              {(user?.avatarUrl && imgOk) ? (
                <img
                  className="avatar-img"
                  src={user.avatarUrl}
                  alt={`${user?.username || user?.email} avatar`}
                  onError={() => setImgOk(false)}
                />
              ) : (
                <span className="avatar-initials">{initials}</span>
              )}
            </button>

            {menuOpen && (
              <div className="profile-menu" role="menu" ref={menuRef}>
                <div className="menu-header">
                  Signed in as <b>{user?.username || user?.email}</b>
                </div>

                <button
                  className="menu-item"
                  role="menuitem"
                  onClick={() => { setMenuOpen(false); setCurrentPage("profile"); }}
                >
                  Profile
                </button>

                <div className="menu-sep" />

                <button className="menu-item danger" role="menuitem" onClick={logout}>
                  Log out
                </button>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* Main */}
      <main className="main-content">
        <section className="hero-section">
          <h1 className="hero-title">Welcome to your dashboard :)</h1>
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