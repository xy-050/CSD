import { useCallback } from 'react';
import { useCurrentUser } from "../auth/useCurrentUser";

export function useSaveQuery() {
    const { user } = useCurrentUser();

    const saveQuery = useCallback(async (htsCode, options = {}) => {
        if (!user.userId) {
            console.debug('No user logged in, skipping query save');
            return;
        }

        try {
            const queryData = {
                userID: { userID: user.userId },
                htsCode: htsCode,
                originCountry: options.originCountry || null,
                modeOfTransport: options.modeOfTransport || null,
                quantity: options.quantity || 0,
            };
            await api.post('/api/tariffs/queries', queryData);
        } catch (error) {
            console.error('Failed to save query:', error);
        }
    }, [user.userId]);

    return saveQuery;
}
