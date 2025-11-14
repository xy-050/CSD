import { useState, useEffect, useCallback } from 'react';
import api from '../../api/AxiosConfig.jsx';

export function useFavorite(userId, htsCode) {
    const [isFavorite, setIsFavorite] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Check if item is favorited
    useEffect(() => {
        const checkFavoriteStatus = async () => {
            if (!userId || !htsCode) return;

            try {
                const res = await api.get(`/accounts/${userId}/favourites`);
                const list = Array.isArray(res.data) ? res.data : (res.data?.items || []);
                setIsFavorite(list.some(item => item.htsno === htsCode));
            } catch (err) {
                console.error('Error checking favourite status:', err);
                setError(err);
            }
        };

        checkFavoriteStatus();
    }, [userId, htsCode]);

    // Toggle favorite status
    const toggleFavorite = useCallback(async () => {
        if (!htsCode || loading) return;

        setLoading(true);
        setError(null);

        try {
            if (isFavorite) {
                await api.delete(`/accounts/${userId}/favourites`, {
                    params: { htsCode }
                });
                setIsFavorite(false);
            } else {
                await api.post(`/accounts/${userId}/favourites`, null, {
                    params: { htsCode }
                });
                setIsFavorite(true);
            }
        } catch (err) {
            console.error('Error toggling favourite:', err);
            setError(err);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [userId, htsCode, isFavorite, loading]);

    return { isFavorite, loading, error, toggleFavorite };
}
