import { useState, useRef, useEffect } from "react";
import NavBar from "./NavBar";
import SearchBar from "./SearchBar";
import Sidebar from "./Sidebar";

export default function HomePage() {
    const [lastQuery, setLastQuery] = useState("");
    const [sidebarOpen, setSidebarOpen] = useState(window.innerWidth > 768);
    const [typedTitle, setTypedTitle] = useState("");
    const [doneTyping, setDoneTyping] = useState(false);

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
            <NavBar 
                onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} 
                sidebarOpen={sidebarOpen}
            />
            
            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} onToggle={() => setSidebarOpen(!sidebarOpen)} />
                
                {/* Main */}
                <main className="main-content">
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
                </main>
            </div>
        </div>
    );
}