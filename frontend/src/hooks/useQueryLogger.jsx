import { useCallback } from 'react';
import api from '../api/AxiosConfig.jsx';

export function useQueryLogger(userId) {
    const logQuery = useCallback(async (htsCode, originCountry = null, modeOfTransport = null, quantity = 0) => {
        if (!userId) return { success: false };

        try {
            const queryData = {
                userID: { userID: userId },
                htsCode,
                originCountry,
                modeOfTransport,
                quantity,
            };

            await api.post('/api/tariffs/queries', queryData);
            return { success: true };
        } catch (error) {
            console.error('Failed to save query:', error);
            return { success: false, error };
        }
    }, [userId]);

    return { logQuery };
}
