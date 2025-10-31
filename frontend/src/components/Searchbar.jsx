import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/AxiosConfig.jsx";

/*
 * TODO:
 * - understand lines before handleSearch function
 * - update for search history -> should store/call from Query database to update/display to segregate between users
 * - understand what is debounce (live search)
 * - get rid of user 
 */

export default function SearchBar({ }) {
    const [q, setQ] = useState("");
    // const [history, setHistory] = useState([]); // commented - re-enable if needed
    const [results, setResults] = useState([]);
    const [error, setError] = useState(null);
    const inputRef = useRef(null);
    const timerRef = useRef(null);
    const navigate = useNavigate();

    // add category definitions
    const categories = [
        { key: 'Sugar', label: 'Sugar', icon: '🍬' },
        { key: 'Bread', label: 'Bread', icon: '🍞' },
        { key: 'Milk', label: 'Milk', icon: '🥛' },
        { key: 'Egg', label: 'Egg', icon: '🥚' },
        { key: 'Rice', label: 'Rice', icon: '🍚' },
    ];

    // // Toggle to enable mock data locally (set to false to call real backend)
    // const useMock = useMemo(() => true, []);

    // // Small mock dataset shaped like backend results used by SearchResults
    // const mockProducts = useMemo(() => ([
    //     {
    //         id: 'sugar-1',
    //         name: 'White Sugar 1kg',
    //         category: 'Sugar',
    //         price: 2.5,
    //         htsno: '1701.11',
    //         // descriptionChain is an array of progressively specific descriptions
    //         descriptionChain: ['Sugar and sugar substitutes', 'Refined white sugar', 'White Sugar 1kg package'],
    //         general: '5%', // some tariff text the UI checks for
    //         units: 'kg'
    //     },
    //     {
    //         id: 'sugar-2',
    //         name: 'Brown Sugar 500g',
    //         category: 'Sugar',
    //         price: 1.9,
    //         htsno: '1701.19',
    //         descriptionChain: ['Sugar and sugar substitutes', 'Brown sugar', 'Brown Sugar 500g pack'],
    //         general: '', // simulate case with no general tariff (see more behavior)
    //         units: 'pack'
    //     },
    //     {
    //         id: 'bread-1',
    //         name: 'Sourdough Loaf',
    //         category: 'Bread',
    //         price: 3.2,
    //         htsno: '1905.31',
    //         descriptionChain: ['Bakery products', 'Bread', 'Sourdough Loaf'],
    //         general: '0%',
    //         units: 'loaf'
    //     },
    //     {
    //         id: 'milk-1',
    //         name: 'Whole Milk 1L',
    //         category: 'Milk',
    //         price: 1.2,
    //         htsno: '0401.20',
    //         descriptionChain: ['Milk and cream', 'Milk, whole', 'Whole Milk 1L'],
    //         general: '10%',
    //         units: 'L'
    //     },
    //     {
    //         id: 'egg-1',
    //         name: 'Free Range Eggs 12pc',
    //         category: 'Egg',
    //         price: 2.8,
    //         htsno: '0407.00',
    //         descriptionChain: ['Eggs', 'Chicken eggs, in shell', 'Free Range Eggs 12pc'],
    //         general: '',
    //         units: 'dozen'
    //     },
    //     {
    //         id: 'rice-1',
    //         name: 'Basmati Rice 1kg',
    //         category: 'Rice',
    //         price: 4.5,
    //         htsno: '1006.30',
    //         descriptionChain: ['Rice', 'Basmati rice', 'Basmati Rice 1kg'],
    //         general: '2%',
    //         units: 'kg'
    //     },
    // ]), []);

    // // per-user storage key (commented out)
    // const storageKey = useMemo(
    //     () => `history:${user?.email || "anon"}`,
    //     [user?.email]
    // );
    //
    // // safe JSON helpers (commented out)
    // const readSearchHistory = () => {
    //     try { return JSON.parse(localStorage.getItem(storageKey) || "[]"); }
    //     catch { return []; }
    // };
    // const writeSearchHistory = (arr) =>
    //     localStorage.setItem(storageKey, JSON.stringify(arr));
    //
    // // load history when user (key) changes
    // useEffect(() => { setHistory(readSearchHistory()); }, [storageKey]);
    //
    // const saveHistory = (arr) => { setHistory(arr); writeSearchHistory(arr); };
    //
    // const removeOne = (term) => saveHistory(history.filter(x => x !== term));
    // const clearAll = () => saveHistory([]);

    const handleSearch = async (term) => {
        const searchTerm = (term ?? q).trim();

        // If no search term, go directly to the results page (no API call)
        if (!searchTerm) {
            navigate("/results", { state: { results: [], keyword: "" } });
            return;
        }

        // // Local mock mode: filter the mock dataset and return results (case-insensitive)
        // if (useMock) {
        //     // simulate network delay
        //     await new Promise((r) => setTimeout(r, 200));
        //     const filtered = mockProducts.filter(p =>
        //         (p.category || '').toLowerCase() === searchTerm.toLowerCase()
        //         || (p.name || '').toLowerCase().includes(searchTerm.toLowerCase())
        //         || (p.htsno || '').toLowerCase().includes(searchTerm.toLowerCase())
        //     );
        //     // mockProducts already follow the expected result shape so we can pass through
        //     setResults(filtered);
        //     navigate("/results", { state: { results: filtered, keyword: searchTerm } });
        //     return;
        // }

        // Clear any previous errors
        setError(null);

        try {
            console.log('Starting search for:', searchTerm);

            // Call the new category endpoint which returns HTS codes for the category.
            // We use encodeURIComponent to safely place the category into the URL path.
            const response = await api.get(`product/category/${encodeURIComponent(searchTerm)}`);

            console.log(response);
            console.log(response.data);

            // backend may return { products: [...] } or the list directly — handle both.
            const data = response.data?.products ?? response.data;

            setResults(data || []);
            navigate("/results", { state: { results: data || [], keyword: searchTerm } });
        } catch (error) {
            console.error('Error during search:', error);
            setError('Search failed. Please check your connection and try again.');
            setResults([]); // Clear results on error
        }
    };

    return (
        <section className="search-section">
            <div
                className="search-bar"
                role="search"
                aria-label="Site search"
            >
                {/* Replaced input + generic button with 5 category buttons */}
                <div
                    style={{
                        display: 'flex',
                        justifyContent: 'space-evenly', // spread across the screen
                        gap: '1.25rem',
                        padding: '2rem 1rem',
                        flexWrap: 'wrap',
                        width: '100%',
                        maxWidth: 1100,
                        margin: '0 auto' // center the whole block
                    }}
                >
                    {categories.map(cat => (
                        <button
                            key={cat.key}
                            type="button"
                            className="btn-primary search-btn"
                            onClick={() => handleSearch(cat.key)}
                            aria-label={`Search ${cat.label}`}
                            style={{
                                minWidth: 140,           // larger buttons
                                padding: '1rem 1.25rem',
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                gap: '0.35rem',
                                borderRadius: 10,
                                cursor: 'pointer',
                                fontSize: '1rem',
                                boxShadow: '0 2px 6px rgba(0,0,0,0.08)'
                            }}
                        >
                            <span style={{ fontSize: '2rem', lineHeight: 1 }}>{cat.icon}</span>
                            <span style={{ fontSize: '1rem', fontWeight: 600 }}>{cat.label}</span>
                        </button>
                    ))}
                </div>
            </div>

            {/* Show error if it exists */}
            {error && (
                <div className="search-error" style={{ color: 'red', padding: '10px' }}>
                    {error}
                </div>
            )}

            {/* 
                Commented-out history UI: keep for other developers to re-enable if needed.
                To restore, uncomment the state + helpers above and this block below.
            */}
            {/*
            {history.length > 0 && (
                <div className="search-history">
                    <div className="history-header">
                        <span>Recent searches</span>
                        <button className="link-btn" onClick={clearAll}>Clear all</button>
                    </div>

                    <div className="chips">
                        {history.map((term) => (
                            <button
                                key={term}
                                className="chip"
                                onClick={() => handleSearch(term)}
                                title={`Search "${term}"`}
                            >
                                <span className="chip-text">{term}</span>
                                <span
                                    className="chip-x"
                                    role="button"
                                    aria-label={`Remove ${term}`}
                                    onClick={(e) => { e.stopPropagation(); removeOne(term); }}
                                >
                                    ×
                                </span>
                            </button>
                        ))}
                    </div>
                </div>
            )}
            */}

        </section>
    );
}