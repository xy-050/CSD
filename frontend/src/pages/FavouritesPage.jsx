import { useNavigate } from "react-router-dom";

import NavBar from "../components/layout/NavBar/NavBar";
import Sidebar from "../components/layout/SideBar/Sidebar";
import { FavouritesGrid } from '../components/features/favourites/FavouritesGrid';
import { EmptyFavourites } from '../components/features/favourites/EmptyFavourites';
import { LoadingFavourites } from '../components/features/favourites/LoadingFavourites';

import { useResponsiveSidebar } from '../hooks/ui/useResponsiveSidebar.jsx';
import { useCurrentUser } from "../hooks/auth/useCurrentUser.jsx";
import { useFavourites } from '../hooks/favourites/useFavourites.jsx';
import { useQueryLogger } from '../hooks/search/useQueryLogger.jsx';

import { createCalculatorState } from '../utils/favouriteFormatter';

export default function FavouritesPage() {
    const navigate = useNavigate();

    // Sidebar management
    const {
        isOpen: sidebarOpen,
        toggle: toggleSidebar,
        closeOnMobile: closeSidebar
    } = useResponsiveSidebar();

    // User management
    const { user } = useCurrentUser();

    // Favourites management
    const {
        favourites,
        loading,
        error,
        removeFavourite
    } = useFavourites(user.userId);

    // Query logging
    const { logQuery } = useQueryLogger(user.userId);

    // Handle remove favourite with confirmation
    const handleRemoveFavourite = async (htsCode) => {
        const confirmed = window.confirm(
            `Are you sure you want to remove ${htsCode} from favourites?`
        );

        if (!confirmed) return;

        const result = await removeFavourite(htsCode);

        if (!result.success) {
            alert("Failed to remove favourite. Please try again.");
        }
    };

    // Navigate to calculator with favourite data
    const handleGoToCalculator = async (favourite) => {
        // Log the query
        await logQuery(favourite.htsCode);

        // Navigate to calculator
        navigate("/calculator", {
            state: createCalculatorState(favourite)
        });
    };

    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />

            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />

                <main className="main-content" onClick={closeSidebar}>
                    <section className="hero-section">
                        <h1 className="hero-title">⭐ Your Favourites</h1>
                        <p className="hero-subtitle">
                            View your saved goods and re-run tariff simulations instantly.
                        </p>
                        {favourites.length > 0 && (
                            <div className="favourites-count">
                                {favourites.length} {favourites.length === 1 ? 'item' : 'items'} saved
                            </div>
                        )}
                    </section>

                    {error && (
                        <div className="error-banner">
                            <span className="error-icon">⚠️</span>
                            <span>{error}</span>
                        </div>
                    )}

                    {loading ? (
                        <LoadingFavourites />
                    ) : favourites.length === 0 ? (
                        <EmptyFavourites />
                    ) : (
                        <FavouritesGrid
                            favourites={favourites}
                            onRemove={handleRemoveFavourite}
                            onCardClick={handleGoToCalculator}
                        />
                    )}
                </main>
            </div>
        </div>
    );
}
