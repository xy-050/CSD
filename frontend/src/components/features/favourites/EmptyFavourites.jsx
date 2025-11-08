import { useNavigate } from 'react-router-dom';

export function EmptyFavourites() {
    const navigate = useNavigate();

    return (
        <div className="empty-favourites">
            <h2 className="empty-title">No Favourites Yet</h2>
            <p className="empty-subtitle">
                Start adding your frequently used HTS codes to quickly access them later.
            </p>
            <button
                className="explore-btn"
                onClick={() => navigate('/home')}
            >
                Explore HTS Codes
            </button>
        </div>
    );
}
