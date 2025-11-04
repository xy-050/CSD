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
        { key: 'Sugar', label: 'Sugar', icon: '🍬', dataTour: null},
        { key: 'Bread', label: 'Bread', icon: '🍞', dataTour: null},
        { key: 'Milk', label: 'Milk', icon: '🥛', dataTour: 'category-milk'},
        { key: 'Egg', label: 'Egg', icon: '🥚', dataTour: null},
        { key: 'Rice', label: 'Rice', icon: '🍚', dataTour: null},
    ];

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

        // Clear any previous errors
        setError(null);

        try {
            console.log('Starting search for:', searchTerm);

            const response = await api.get(`/product/category/${searchTerm}`, {
                params: {
                    keyword: encodeURIComponent(searchTerm)
                }
            });

            console.log(response);
            console.log(response.data);
            setResults(response.data);
            navigate("/results", { state: { results: response.data, keyword: searchTerm } });
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
                {/* Category buttons with data-tour attributes */}
                <div
                    data-tour="category-buttons"
                    style={{
                        display: 'flex',
                        justifyContent: 'space-evenly',
                        gap: '1.25rem',
                        padding: '2rem 1rem',
                        flexWrap: 'wrap',
                        width: '100%',
                        maxWidth: 1100,
                        margin: '0 auto'
                    }}
                >
                    {categories.map(cat => (
                        <button
                            key={cat.key}
                            type="button"
                            className="btn-primary search-btn"
                            onClick={() => handleSearch(cat.key)}
                            aria-label={`Search ${cat.label}`}
                            {...(cat.dataTour && { 'data-tour': cat.dataTour })}
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