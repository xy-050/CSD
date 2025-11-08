import { useState, useEffect, useCallback } from 'react';
import { getName as IsoToName } from 'country-list';
import api from '../api/AxiosConfig.jsx';

// Helper to extract numeric rate from strings like "5.6%" or "Free"
function extractNumericRate(rateString) {
    if (!rateString || rateString === 'Free' || rateString === 'N/A') return 0;
    const match = rateString.match(/([0-9.]+)/);
    return match ? parseFloat(match[1]) : 0;
}

export function useCountryTariffs(htsCode) {
    const [countryTariffs, setCountryTariffs] = useState(null);
    const [availableCountries, setAvailableCountries] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCountryTariffs = async () => {
            if (!htsCode) return;

            setLoading(true);
            setError(null);

            try {
                const response = await api.get(`product/price/map/${htsCode}`);
                const data = response.data;
                console.log("Country tariffs data:", data);

                setCountryTariffs(data);

                // Convert to country list
                const countries = [];
                Object.entries(data).forEach(([code, rate]) => {
                    const countryName = IsoToName(code);
                    if (countryName) {
                        countries.push({
                            name: countryName,
                            code: code,
                            rate: rate
                        });
                    }
                });

                // Sort alphabetically
                countries.sort((a, b) => a.name.localeCompare(b.name));
                setAvailableCountries(countries);

            } catch (err) {
                console.error('Error fetching country tariffs:', err);
                setError(err);
            } finally {
                setLoading(false);
            }
        };

        fetchCountryTariffs();
    }, [htsCode]);

    // Get tariff rate for a specific country
    const getTariffRate = useCallback((countryName) => {
        const found = availableCountries.find(c => c.name === countryName);
        return found ? found.rate : (countryTariffs?.['General rate'] || 'N/A');
    }, [availableCountries, countryTariffs]);

    // Get numeric rate
    const getNumericRate = useCallback((countryName) => {
        const rate = getTariffRate(countryName);
        return extractNumericRate(rate);
    }, [getTariffRate]);

    return {
        countryTariffs,
        availableCountries,
        loading,
        error,
        getTariffRate,
        getNumericRate
    };
}
