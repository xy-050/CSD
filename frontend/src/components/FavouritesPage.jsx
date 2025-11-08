import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar";
import Sidebar from "./Sidebar";
import api from "../api/AxiosConfig.jsx";

export default function FavouritesPage() {
    const [favourites, setFavourites] = useState([]);
    const [userID, setUserID] = useState("");
    const [loading, setLoading] = useState(true);

    // sidebar state
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const toggleSidebar = () => setSidebarOpen(v => !v);
    const closeSidebar = () => setSidebarOpen(false);

    const navigate = useNavigate();

    // fetch user id
    useEffect(() => {
        (async () => {
            try {
                const res = await api.get("/currentUserDetails");
                setUserID(res.data.userId);
            } catch (err) {
                console.error("Error fetching user:", err);
            }
        })();
    }, []);

    // fetch favourites
    useEffect(() => {
        if (!userID) return;
        (async () => {
            try {
                setLoading(true);
                const res = await api.get(`/account/${userID}/favourites`);
                const data = res.data;
                console.log(data);
                setFavourites(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Error fetching favourites:", err);
            } finally {
                setLoading(false);
            }
        })();
    }, [userID]);

    useEffect(() => {
        console.log("Favourites updated:", favourites);
        favourites.map(fav => console.log(fav));
    }, [favourites]);

    const handleUnfavourite = async (htsCode) => {
        try {
            await api.delete(`/account/${userID}/favourites`, { params: { htsCode } });
            setFavourites(prev => prev.filter(item => item.htsCode !== htsCode));  // ← Changed from htsno
        } catch (err) {
            console.error("Error removing favourite:", err);
            alert("Failed to remove favourite. Please try again.");
        }
    };


    const goToCalculator = async (fav) => {
        // Save query
        if (userID) {
            try {
                const queryData = {
                    userID: { userID: userID },
                    htsCode: fav.htsCode,
                    originCountry: null,
                    modeOfTransport: null,
                    quantity: 0,
                };
                await api.post('/api/tariffs/queries', queryData);
            } catch (error) {
                console.error('Failed to save query:', error);
            }
        }

        // Format result with all fields from DTO
        const formattedResult = {
            htsno: fav.htsCode,
            description: fav.description,
            category: fav.category,
            general: fav.general,
            special: fav.special,
            units: fav.units,
            descriptionChain: [fav.description],
            fullDescriptionChain: [fav.description]
        };

        navigate("/calculator", {
            state: {
                result: formattedResult,
                keyword: fav.category
            },
        });
    };

    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />

            {/* Layout row: sidebar + content */}
            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />                  {/* ← sidebar mounted */}

                <main className="main-content" onClick={closeSidebar}>
                    <section className="hero-section">
                        <h1 className="hero-title">⭐ Your Favourites</h1>
                        <p className="hero-subtitle">
                            View your saved goods and re-run tariff simulations instantly.
                        </p>
                    </section>

                    {loading ? (
                        <div className="loading">Loading favourites...</div>
                    ) : favourites.length === 0 ? (
                        <div className="empty-state">
                            <p>You haven’t added any favourites yet.</p>
                            <p>Go explore goods in the Calculator page!</p>
                        </div>
                    ) : (
                        <div className="favourites-grid">
                            {favourites.map((fav, i) => (
                                <div key={fav.htsCode} className="fav-card" onClick={() => goToCalculator(fav)}>
                                    <div className="fav-card-header">
                                        <div className="fav-info">
                                            <h3>HTS: {fav.htsCode}</h3>
                                            {fav.category && <p className="category">{fav.category}</p>}
                                            <p className="muted">{fav.description}</p>
                                        </div>
                                        <button
                                            type="button"
                                            className="star-btn active"
                                            title="Remove from favourites"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleUnfavourite(fav.htsCode);
                                            }}
                                        >
                                            ★
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
}
