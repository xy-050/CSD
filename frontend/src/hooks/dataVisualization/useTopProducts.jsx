import { useState, useEffect } from 'react';
import api from "../../api/AxiosConfig";

export function useTopProducts(limit = 10) {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchTopProducts = async () => {
            try {
                setLoading(true);
                setError(null);
                const res = await api.get("/api/tariffs/most-queried");
                const list = Array.isArray(res.data) ? res.data : [];

                // Filter out invalid entries
                const valid = list.filter(
                    item => item && item.htsCode && item.htsCode.trim() !== ""
                );

                setProducts(valid.slice(0, limit));
            } catch (err) {
                console.error("Failed to load top products", err);
                setError("Could not load top products");
            } finally {
                setLoading(false);
            }
        };

        fetchTopProducts();
    }, [limit]);

    return { products, loading, error };
}
