import { useState, useEffect } from 'react';
import api from '../api/AxiosConfig.jsx';

export function useSearchResults(keyword, navigationResults) {
    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchResults = async () => {
            console.log("Fetching results for keyword:", keyword);

            // If we have results from navigation state, use those
            if (navigationResults) {
                setResults(navigationResults);
                setLoading(false);
                return;
            }

            // If no keyword, show empty results
            if (!keyword) {
                setResults([]);
                setLoading(false);
                return;
            }

            try {
                const response = await api.get(
                    `/product/category/search/${encodeURIComponent(keyword)}`
                );

                if (response.data.categories) {
                    console.log("API Response:", response.data);

                    // Transform categories into results format
                    const formattedResults = Array.from(response.data.categories).map(product => ({
                        htsno: product.htsCode || product,
                        descriptionChain: [product.description || product],
                        general: product.general || "",
                        category: product.category,
                        special: product.special || "",
                        units: product.units || ""
                    }));

                    console.log("Formatted results:", formattedResults);
                    setResults(formattedResults);
                } else {
                    setResults([]);
                }
            } catch (err) {
                console.error('Error fetching results:', err);
                setError('Failed to fetch results. Please try again.');
                setResults([]);
            } finally {
                setLoading(false);
            }
        };

        setLoading(true);
        setError(null);
        fetchResults();
    }, [keyword, navigationResults]);

    return { results, loading, error };
}
