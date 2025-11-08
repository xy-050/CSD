import { useMemo } from 'react';

export function useTourSpotlight(results, tourActive) {
    return useMemo(() => {
        if (!tourActive || !Array.isArray(results)) {
            return { parentIndex: undefined, spotlightIndex: undefined };
        }

        const displayedResults = results.slice(0, 8);

        // Find indexes of items with general tariff
        const generalTariffIndexes = displayedResults
            .map((r, i) => (r.general && r.general.trim() !== '' ? i : null))
            .filter(i => i !== null);

        // First box is always the parent
        const parentIndex = 0;

        // Second item with general tariff is the spotlight
        const spotlightIndex = generalTariffIndexes.length > 1
            ? generalTariffIndexes[1]
            : generalTariffIndexes[0];

        return { parentIndex, spotlightIndex };
    }, [results, tourActive]);
}
