import { useState, useEffect } from 'react';

export const useCountryGeoJSON = () => {
    const [geoData, setGeoData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchGeoData = async () => {
            try {
                const response = await fetch(
                    'https://raw.githubusercontent.com/datasets/geo-countries/master/data/countries.geojson'
                );
                const data = await response.json();
                setGeoData(data);
            } catch (err) {
                console.error('Error fetching GeoJSON:', err);
                setError(err);
            } finally {
                setLoading(false);
            }
        };

        fetchGeoData();
    }, []);


    return { geoData, loading, error };
};
