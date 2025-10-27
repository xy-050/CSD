import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/AxiosConfig.jsx";
import johnpork from "../assets/johnpork.png";

export default function NavBar({ }) {
    const [menuOpen, setMenuOpen] = useState(false);
    const [username, setUsername] = useState(null);
    const [initials, setInitials] = useState(null);
    const menuRef = useRef(null);
    const btnRef = useRef(null);
    const [imgOk, setImgOk] = useState(true);
    const navigate = useNavigate();

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

    useEffect(() => {
        const getUsername = async () => {
            try {
                const response = await api.get("/currentUserDetails");
                console.log(response);
                setUsername(response.data.username);
            } catch (error) {
                console.log(error);
            }
        }
        getUsername();
    }, []);

    useEffect(() => {
        if (username) {
            setInitials(username.split(/\s+/).map(s => s[0]).slice(0, 2).join("").toUpperCase())
        }
    })

    const handleLogout = () => {
        localStorage.removeItem("token");
        navigate("/login");
    }

    return (
        <nav className="navbar">
            <div className="nav-container">
                {/* Brand -> go home */}
                <button
                    className="nav-brand"
                    style={{ background: "none", border: 0, cursor: "pointer" }}
                    onClick={() => navigate("/")}
                    aria-label="Go to dashboard"
                >
                    <span className="nav-icon">💵</span>
                    <span className="nav-title">Tariff-ic!</span>
                </button>

                {/* Avatar + dropdown */}
                <div className="nav-user" style={{ position: "relative" }}>
                    <span className="welcome-text">Hi, <b>{username}</b></span>
                    <button
                        ref={btnRef}
                        className="avatar-btn"
                        aria-haspopup="menu"
                        aria-expanded={menuOpen}
                        title={username}
                        onClick={() => setMenuOpen(v => !v)}
                    >
                        {(johnpork && imgOk)
                            ? <img className="avatar-img" src={johnpork} alt="avatar" onError={() => setImgOk(false)} />
                            : <span className="avatar-initials">{initials}</span>}
                    </button>

                    {menuOpen && (
                        <div className="profile-menu" role="menu" ref={menuRef}>
                            <div className="menu-header">Signed in as <b>{username}</b></div>
                            <button
                                className="menu-item"
                                role="menuitem"
                                onClick={() => { setMenuOpen(false); navigate("/profile"); }}
                            >
                                Profile
                            </button>
                            <div className="menu-sep" />
                            <button className="menu-item danger" role="menuitem" onClick={handleLogout}>
                                Log out
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
}