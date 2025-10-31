import { useState, useRef, useEffect } from "react";
import NavBar from "./NavBar";
import SearchBar from "./SearchBar";
import axios from "../api/AxiosConfig";
import Sidebar from "./Sidebar";

export default function HomePage() {
    const [lastQuery, setLastQuery] = useState("");
    const [sidebarOpen, setSidebarOpen] = useState(window.innerWidth > 768);
    const [typedTitle, setTypedTitle] = useState("");
    const [doneTyping, setDoneTyping] = useState(false);
    const [topProducts, setTopProducts] = useState([]);
    const [topLoading, setTopLoading] = useState(false);
    const [topError, setTopError] = useState(null);

    // Check if mobile on mount
    useEffect(() => {
        const isMobile = window.innerWidth <= 768;
        setSidebarOpen(!isMobile); // Open on desktop, closed on mobile
    }, []);

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
        }, 60);
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

    // handle escape key to close sidebar on mobile
    useEffect(() => {
        const onKey = (e) => {
            if (e.key === "Escape" && window.innerWidth <= 768) {
                setSidebarOpen(false);
            }
        };
        document.addEventListener("keydown", onKey);
        return () => document.removeEventListener("keydown", onKey);
    }, []);

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
            </main>
        </div>
    );
}