import { useState, useRef, useEffect } from "react";
import NavBar from "./NavBar";
import SearchBar from "./SearchBar";

export default function HomePage({ onSearch, user, setUser, setCalcQuery }) {
    const [lastQuery, setLastQuery] = useState("");
    const menuRef = useRef(null)
    const [typedTitle, setTypedTitle] = useState("");
    const [doneTyping, setDoneTyping] = useState(false);

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
                    <SearchBar
                        user={user}
                    />
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
            </main>
        </div>
    )
}