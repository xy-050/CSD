import { useNavigate } from 'react-router-dom';

export function EmptyCalculator() {
    const navigate = useNavigate();

    return (
        <div className="empty-calculator">
            <div className="empty-icon">📊</div>
            <h2 className="empty-title">No Tariff Data Available</h2>
            <p className="empty-subtitle">
                Please go back and select an item to calculate duties.
            </p>
            <button
                className="back-to-search-btn"
                onClick={() => navigate('/home')}
            >
                Back to Search
            </button>
        </div>
    );
}
