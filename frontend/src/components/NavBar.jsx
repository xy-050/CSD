import { useEffect, useRef, useState } from "react";

export default function NavBar({ user, setUser, setCurrentPage }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);
  const btnRef  = useRef(null);
  const [imgOk, setImgOk] = useState(true);

  const initials = (user?.username || user?.email || "U")
    .split(/\s+/).map(s => s[0]).slice(0, 2).join("").toUpperCase();

  const logout = () => { setUser(null); setCurrentPage("login"); };

  // close dropdown on outside click / Esc
  useEffect(() => {
    const onDown = (e) => {
      if (!menuRef.current || !btnRef.current) return;
      if (!menuRef.current.contains(e.target) && !btnRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    const onKey = (e) => { if (e.key === "Escape") setMenuOpen(false); };
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDown);
      document.removeEventListener("keydown", onKey);
    };
  }, []);

  return (
    <nav className="navbar">
      <div className="nav-container">
        {/* Brand -> go home */}
        <button
          className="nav-brand"
          style={{ background: "none", border: 0, cursor: "pointer" }}
          onClick={() => setCurrentPage("home")}
          aria-label="Go to dashboard"
        >
          <span className="nav-icon">💵</span>
          <span className="nav-title">Tariff-ic!</span>
        </button>

        {/* Avatar + dropdown */}
        <div className="nav-user" style={{ position: "relative" }}>
          <span className="welcome-text">Hi, <b>{user?.username}</b></span>
          <button
            ref={btnRef}
            className="avatar-btn"
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            title={user?.username || user?.email}
            onClick={() => setMenuOpen(v => !v)}
          >
            {(user?.avatarUrl && imgOk)
              ? <img className="avatar-img" src={user.avatarUrl} alt="avatar" onError={() => setImgOk(false)} />
              : <span className="avatar-initials">{initials}</span>}
          </button>

          {menuOpen && (
            <div className="profile-menu" role="menu" ref={menuRef}>
              <div className="menu-header">Signed in as <b>{user?.username || user?.email}</b></div>
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
  );
}