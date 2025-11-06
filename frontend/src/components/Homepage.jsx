import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar";
import SearchBar from "./Searchbar";
import Sidebar from "./Sidebar";
import api from "../api/AxiosConfig.jsx";

export default function HomePage() {
  const navigate = useNavigate();

  const [lastQuery, setLastQuery] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(window.innerWidth > 768);
  const [typedTitle, setTypedTitle] = useState("");
  const [doneTyping, setDoneTyping] = useState(false);

  // (optional) for future use
  const [topProducts, setTopProducts] = useState([]);
  const [topLoading, setTopLoading] = useState(false);
  const [topError, setTopError] = useState(null);

  const toggleSidebar = () => setSidebarOpen(v => !v);
  const closeSidebarOnMobile = () => {
    if (window.innerWidth <= 768) setSidebarOpen(false);
  };

  // Open by default on desktop, closed on mobile
  useEffect(() => {
    const isMobile = window.innerWidth <= 768;
    setSidebarOpen(!isMobile);
  }, []);

  // ESC closes sidebar on mobile
  useEffect(() => {
    const onKey = (e) => {
      if (e.key === "Escape" && window.innerWidth <= 768) {
        setSidebarOpen(false);
      }
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, []);

  // Typing effect
  useEffect(() => {
    const fullTitle = "Welcome to your dashboard :)";
    let i = 0;
    const id = setInterval(() => {
      i += 1;
      setTypedTitle(fullTitle.slice(0, i));
      if (i >= fullTitle.length) {
        clearInterval(id);
        setDoneTyping(true);
      }
    }, 60);
    return () => clearInterval(id);
  }, []);

  // Fetch top queried products (single effect; your duplicate removed)
  useEffect(() => {
    (async () => {
      try {
        setTopLoading(true);
        setTopError(null);
        const res = await api.get("/api/tariffs/most-queried");
        const list = Array.isArray(res.data) ? res.data : [];
        const valid = list.filter(code => code && code.trim() !== "");
        setTopProducts(valid.slice(0, 10));
      } catch (err) {
        console.error("Failed to load top products", err);
        setTopError("Could not load top products");
      } finally {
        setTopLoading(false);
      }
    })();
  }, []);

  return (
    <div className="homepage">
      <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />

      <div className="homepage-container">
        {/* Sidebar */}
        <Sidebar isOpen={sidebarOpen} />

        {/* Main */}
        <main className="main-content" onClick={closeSidebarOnMobile}>
          <section className="hero-section">
            <h1 className="hero-title">
              {typedTitle}
              {!doneTyping && <span className="caret" aria-hidden="true" />}
            </h1>

            {/* SearchBar */}
            <SearchBar />

            {lastQuery && (
              <p className="hero-subtitle" style={{ marginTop: "0.75rem" }}>
                Showing results for: <b>{lastQuery}</b>
              </p>
            )}
          </section>

          {/* These feature cards are fine; wired to routes for convenience */}
          <section className="features-grid">
            <article className="feature-card">
              <div className="feature-header">
                <div className="feature-icon blue">📈</div>
                <h3>Insights</h3>
              </div>
              <p>Track activity and recent events at a glance.</p>
              <button className="feature-btn" onClick={() => navigate("/home")}>
                View reports
              </button>
            </article>
			<article className="feature-card">
              <div className="feature-header">
                <div className="feature-icon green">⭐️</div>
                <h3>Favourites</h3>
              </div>
              <p>Organize work with a soothing, minimal UI.</p>
              <button className="feature-btn" onClick={() => navigate("/favourites")}>
                Open favourites
              </button>
            </article>

            <article className="feature-card">
              <div className="feature-header">
                <div className="feature-icon purple">⚙️</div>
                <h3>Settings</h3>
              </div>
              <p>Tune preferences and notification rules.</p>
              <button className="feature-btn" onClick={() => navigate("/profile")}>
                Manage
              </button>
            </article>
          </section>
          {/* Top Products */}
          <section className="top-products">
            <h2>Top 10 Most Queried Products</h2>
            {topLoading && <p>Loading top products…</p>}
            {topError && <p style={{ color: 'red' }}>{topError}</p>}
            {!topLoading && !topError && (
              <>
                {topProducts.length === 0 ? (
                  <p style={{ color: 'var(--muted)', fontStyle: 'italic' }}>No queries yet. Start searching to see popular products!</p>
                ) : (
                  <div className="query-cards-container">
                    {/* Top 3 Row */}
                    {topProducts.slice(0, 3).length > 0 && (
                      <div className="query-cards-row top-three">
                        {/* Reorder: #2, #1, #3 */}
                        {topProducts[1] && (
                          <div key={topProducts[1] + '1'} className="query-card top-three">
                            <div className="query-rank">#2</div>
                            <div className="query-code" onClick={() => handleProductClick(topProducts[1])}>
                              {topProducts[1]}
                            </div>
                          </div>
                        )}
                        {topProducts[0] && (
                          <div key={topProducts[0] + '0'} className="query-card top-three rank-1">
                            <div className="query-rank">#1</div>
                            <div className="query-code" onClick={() => handleProductClick(topProducts[0])}>
                              {topProducts[0]}
                            </div>
                          </div>
                        )}
                        {topProducts[2] && (
                          <div key={topProducts[2] + '2'} className="query-card top-three">
                            <div className="query-rank">#3</div>
                            <div className="query-code" onClick={() => handleProductClick(topProducts[2])}>
                              {topProducts[2]}
                            </div>
                          </div>
                        )}
                      </div>
                    )}

                    {/* Remaining 7 Row */}
                    {topProducts.slice(3, 10).length > 0 && (
                      <div className="query-cards-row remaining">
                        {topProducts.slice(3, 10).map((code, idx) => (
                          <div key={code + idx} className="query-card remaining">
                            <div className="query-rank">#{idx + 4}</div>
                            <div className="query-code" onClick={() => handleProductClick(code)}>
                              {code}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </>
            )}
          </section>
        </main>
      </div>
    </div>
  );
}