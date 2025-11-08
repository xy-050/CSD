/**
 * Formats a favourite item for the calculator
 */
export function formatFavouriteForCalculator(favourite) {
    return {
        htsno: favourite.htsCode,
        description: favourite.description,
        category: favourite.category,
        general: favourite.general,
        special: favourite.special,
        units: favourite.units,
        descriptionChain: [favourite.description],
        fullDescriptionChain: [favourite.description]
    };
}

/**
 * Creates navigation state for calculator
 */
export function createCalculatorState(favourite) {
    return {
        result: formatFavouriteForCalculator(favourite),
        keyword: favourite.category || favourite.description
    };
}
