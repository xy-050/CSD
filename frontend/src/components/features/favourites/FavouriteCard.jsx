export function FavouriteCard({ favourite, onRemove, onClick }) {
    const handleRemove = (e) => {
        e.stopPropagation();
        onRemove(favourite.htsCode);
    };

    return (
        <div className="fav-card" onClick={onClick}>
            <div className="fav-card-header">
                <div className="fav-info">
                    <div className="fav-hts-code">
                        <span className="code-label">HTS:</span>
                        <span className="code-value">{favourite.htsCode}</span>
                    </div>

                    {favourite.category && (
                        <div className="fav-category">{favourite.category}</div>
                    )}

                    <p className="fav-description">{favourite.description}</p>

                    {favourite.general && (
                        <div className="fav-rate">
                            <span className="rate-label">General Rate:</span>
                            <span className="rate-value">{favourite.general}</span>
                        </div>
                    )}
                </div>

                <button
                    type="button"
                    className="star-btn active"
                    title="Remove from favourites"
                    onClick={handleRemove}
                    aria-label={`Remove ${favourite.htsCode} from favourites`}
                >
                    ★
                </button>
            </div>
        </div>
    );
}
