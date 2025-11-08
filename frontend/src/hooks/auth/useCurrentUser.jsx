import { useState, useEffect } from 'react';
import api from "../../api/AxiosConfig";

export function useCurrentUser() {
    const [user, setUser] = useState({
        username: '',
        email: '',
        userId: ''
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const getUserDetails = async () => {
            try {
                const response = await api.get("/currentUserDetails");
                console.log(response);
                setUser({
                    username: response.data.username,
                    email: response.data.email,
                    userId: response.data.userId
                });
            } catch (err) {
                console.log(err);
                const errorMessage = err.response?.data?.message
                    || err.response?.data
                    || "Failed to load user details.";
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };

        getUserDetails();
    }, []);

    return { user, loading, error };
}
