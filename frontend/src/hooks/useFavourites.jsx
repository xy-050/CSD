import { useState, useEffect, useCallback } from 'react';
import api from '../api/AxiosConfig.jsx';

export function useFavourites(userId) {
    const [favourites, setFavourites] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch favourites
    useEffect(() => {
        const fetchFavourites = async () => {
            if (!userId) {
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError(null);

                const res = await api.get(`/account/${userId}/favourites`);
                const data = res.data;

                console.log("Favourites data:", data);
                setFavourites(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Error fetching favourites:", err);
                setError(err.message || "Failed to load favourites");
            } finally {
                setLoading(false);
            }
        };

        fetchFavourites();
    }, [userId]);

    // Remove favourite
    const removeFavourite = useCallback(async (htsCode) => {
        if (!userId) return;

        try {
            await api.delete(`/account/${userId}/favourites`, {
                params: { htsCode }
            });

            setFavourites(prev => prev.filter(item => item.htsCode !== htsCode));
            return { success: true };
        } catch (err) {
            console.error("Error removing favourite:", err);
            setError(err.message || "Failed to remove favourite");
            return { success: false, error: err };
        }
    }, [userId]);

    // Refresh favourites
    const refresh = useCallback(async () => {
        if (!userId) return;

        try {
            setLoading(true);
            const res = await api.get(`/account/${userId}/favourites`);
            const data = res.data;
            setFavourites(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error("Error refreshing favourites:", err);
            setError(err.message || "Failed to refresh favourites");
        } finally {
            setLoading(false);
        }
    }, [userId]);

    return {
        favourites,
        loading,
        error,
        removeFavourite,
        refresh
    };
}
