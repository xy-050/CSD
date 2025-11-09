export function ResultCard({ result, index, tourDataAttr, onAction }) {
    const hasGeneralTariff = result.general && result.general.trim() !== '';
    const buttonText = hasGeneralTariff ? 'Calculate' : 'See More';

    return (
        <article
            className="result-item"
            data-tour={tourDataAttr}
        >
            <h3 className="result-hts">{result.htsno}</h3>

            <div className="description-chain">
                {result.descriptionChain?.length ? (
                    result.descriptionChain.map((desc, i) => (
                        <p key={i} className="description-level">
                            {'→ '.repeat(i)}
                            {desc}
                        </p>
                    ))
                ) : (
                    <p className="no-description">No description available</p>
                )}
            </div>

            {result.general && (
                <p className="tariff-info">
                    <strong>General Tariff:</strong> {result.general}
                </p>
            )}

            {result.units && (
                <p className="units-info">
                    <strong>Units:</strong> {result.units}
                </p>
            )}

            <button
                className="result-action-btn"
                onClick={() => onAction(result)}
            >
                {buttonText}
            </button>
        </article>
    );
}
