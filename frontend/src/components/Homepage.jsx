import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar";
import SearchBar from "./SearchBar";
import axios from "../api/AxiosConfig";

export default function HomePage({  }) {
    const [lastQuery, setLastQuery] = useState("");
    const menuRef = useRef(null)
    const [typedTitle, setTypedTitle] = useState("");
    const [doneTyping, setDoneTyping] = useState(false);
    const [topProducts, setTopProducts] = useState([]);
    const [topLoading, setTopLoading] = useState(false);
    const [topError, setTopError] = useState(null);
    const navigate = useNavigate();

    // typing effect for welcome message
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
        }, 60); // typing speed (ms per char)
        return () => clearInterval(id);
    }, []);

    // fetch top 10 most queried products from backend
    useEffect(() => {
        setTopLoading(true);
        setTopError(null);
        axios.get('/api/tariffs/most-queried')
            .then(res => {
                if (Array.isArray(res.data)) {
                    // Filter out null, undefined, and empty strings
                    const validProducts = res.data.filter(code => code && code.trim() !== '');
                    setTopProducts(validProducts.slice(0, 10));
                } else {
                    setTopProducts([]);
                }
            })
            .catch(err => {
                console.error('Failed to load top products', err);
                setTopError('Could not load top products');
            })
            .finally(() => setTopLoading(false));
    }, []);

    // handle outside clicks and escape key
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

    // Handle click on HTS code - fetch product details and navigate to calculator
    const handleProductClick = async (htsCode) => {
        try {
            console.log('Fetching product details for:', htsCode);
            
            // Search for the specific HTS code to get full product details
            const response = await axios.get('/api/tariffs/search', {
                params: { keyword: htsCode }
            });

            console.log(response.data);

            // Find the exact match for this HTS code
            const product = response.data.find(item => item.htsno === htsCode);
            
            if (product && product.general && product.general.trim() !== '') {
                // Format the result for calculator page
                const formattedResult = {
                    ...product,
                    description: product.descriptionChain && product.descriptionChain.length > 0 
                        ? product.descriptionChain[product.descriptionChain.length - 1]
                        : 'No description available',
                    fullDescriptionChain: product.descriptionChain
                };
                
                // Navigate to calculator with the product details
                navigate("/calculator", { 
                    state: { 
                        result: formattedResult, 
                        keyword: htsCode 
                    } 
                });
            } else {
                console.warn('Product details not found or no general tariff for:', htsCode);
                console.log(response.data);
                // alert('Unable to load calculator for this product. Try searching for it first.');
            }
        } catch (error) {
            console.error('Error fetching product details:', error);
            alert('Failed to load product details. Please try again.');
        }
    };

    

    // display initials from user's name or email
    // const initials = (user?.username || user?.email || "U").split(/\s+/).map(s => s[0]).slice(0, 2).join("").toUpperCase()

    // Handle the search by passing the query to parent (App.jsx)
    // const handleSearch = (searchTerm, results) => {
    //     const term = (searchTerm || "").trim();
    //     if (!term) return;
    //     setLastQuery(term); // Store the last query
    //     setCalcQuery(term);  // Pass query to setCalcQuery (for use in calculator page)

    //     // If onSearch is provided (from App.js), call it
    //     if (typeof onSearch === 'function') {
    //         onSearch(term);
    //     }
    // };

    return (
        <div className="homepage">
            <NavBar />

            {/* Main */}
            <main className="main-content">
                <><section className="hero-section">
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
                                <div className="feature-icon green">⭐️</div>
                                <h3>Favourites</h3>
                            </div>
                            <p>Organize work with a soothing, minimal UI.</p>
                            <button className="feature-btn">Open favourites</button>
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
    )
}
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar";
import SearchBar from "./SearchBar";
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
        </main>
      </div>
    </div>
  );
}
