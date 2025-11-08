export function FavoriteButton({ isFavorite, loading, onToggle, dataTour }) {
    return (
        <button
            type="button"
            className={`star-btn ${isFavorite ? 'active' : ''}`}
            data-tour={dataTour}
            aria-pressed={isFavorite}
            title={isFavorite ? "Remove from favorites" : "Add to favorites"}
            onClick={onToggle}
            disabled={loading}
        >
            {isFavorite ? '★' : '☆'}
        </button>
    );
}
