import { FavouriteCard } from './FavouriteCard';

export function FavouritesGrid({ favourites, onRemove, onCardClick }) {
    return (
        <div className="favourites-grid">
            {favourites.map((fav) => (
                <FavouriteCard
                    key={fav.htsCode}
                    favourite={fav}
                    onRemove={onRemove}
                    onClick={() => onCardClick(fav)}
                />
            ))}
        </div>
    );
}
