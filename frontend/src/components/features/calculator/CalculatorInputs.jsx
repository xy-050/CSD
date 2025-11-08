import { FavoriteButton } from './FavoriteButton';

export function CalculatorInputs({
    keyword,
    htsCode,
    description,
    value,
    onValueChange,
    origin,
    onOriginChange,
    availableCountries,
    loading,
    isFavorite,
    favoriteLoading,
    onToggleFavorite
}) {
    return (
        <section className="calc-card">
            <div className="calc-card-head">
                <div className="calc-title-wrap">
                    <div className="mini-label">Inputs</div>
                    <h3 className="calc-title" data-tour="calc-title">{keyword}</h3>
                </div>
                <FavoriteButton
                    isFavorite={isFavorite}
                    loading={favoriteLoading}
                    onToggle={onToggleFavorite}
                    dataTour="star-button"
                />
            </div>

            <div className="form-row">
                <label>HTS Code</label>
                <input
                    className="search-input readonly"
                    value={htsCode}
                    readOnly
                />
            </div>

            <div className="form-row">
                <label>Commodity Description</label>
                <input
                    className="search-input readonly"
                    value={description}
                    readOnly
                />
            </div>

            <div className="form-row" data-tour="shipment-value">
                <label>Shipment Value (USD)</label>
                <input
                    type="number"
                    className="search-input"
                    value={value}
                    onChange={(e) => onValueChange(e.target.value)}
                    min="0"
                    step="0.01"
                />
            </div>

            <div className="form-row" data-tour="country-origin">
                <label>Country of Origin</label>
                {loading ? (
                    <div className="search-input loading-select">
                        Loading countries...
                    </div>
                ) : (
                    <select
                        className="search-input"
                        value={origin}
                        onChange={(e) => onOriginChange(e.target.value)}
                    >
                        {availableCountries.map((country, index) => (
                            <option key={index} value={country.name}>
                                {country.name} - {country.rate}
                            </option>
                        ))}
                        {availableCountries.length === 0 && (
                            <option value="Mexico">Mexico</option>
                        )}
                    </select>
                )}
            </div>
        </section>
    );
}
