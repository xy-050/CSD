import { useState, useRef, useEffect } from "react";
import NavBar from "./NavBar";
import SearchBar from "./Searchbar"

export default function Homepage({ user, setUser, setCurrentPage, setCalcQuery }) {
  const [lastQuery, setLastQuery] = useState("");
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)
  const btnRef = useRef(null)
  const [imgOk, setImgOk] = useState(true);
  const logout = () => { setUser(null); setCurrentPage('login') };
  const handleSearch = (q) => {
    const term = (q || "").trim();
    if (!term) return;
    setCalcQuery(term);
    setCurrentPage("calculator");
  };
  const fullTitle = "Welcome to your dashboard :)";
  const [typedTitle, setTypedTitle] = useState("");
  const [doneTyping, setDoneTyping] = useState(false);

  useEffect(() => {
    let i = 0;
    const id = setInterval(() => {
      i += 1;
      setTypedTitle(fullTitle.slice(0, i));
      if (i >= fullTitle.length) {
        clearInterval(id);
        setDoneTyping(true);
      }
    }, 60); // typing speed (ms per char)
    return () => clearInterval(id);
  }, []);

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
      <NavBar user={user} setUser={setUser} setCurrentPage={setCurrentPage} />

      {/* Main */}
      <main className="main-content">
        <><section className="hero-section">
          <h1 className="hero-title">
            {typedTitle}
            {!doneTyping && <span className="caret" aria-hidden="true" />}
          </h1>
          {/* SearchBar */}
          <SearchBar user={user} onSearch={handleSearch} />
          {lastQuery && (
            <p className="hero-subtitle" style={{ marginTop: "0.75rem" }}>
              Showing results for: <b>{lastQuery}</b>
            </p>
          )}
        </section><section className="features-grid">
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
          </section></>
      </main>
    </div>
  )
}