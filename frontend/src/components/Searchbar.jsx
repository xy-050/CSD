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
    const [history, setHistory] = useState([]);
    const [results, setResults] = useState([]);
    const [error, setError] = useState(null);
    const inputRef = useRef(null);
    const timerRef = useRef(null);
    const navigate = useNavigate();

    // per-user storage key
    // const storageKey = useMemo(
    //     () => `history:${user?.email || "anon"}`,
    //     [user?.email]
    // );

    // // safe JSON
    // const readSearchHistory = () => {
    //     try { return JSON.parse(localStorage.getItem(storageKey) || "[]"); }
    //     catch { return []; }
    // };
    // const writeSearchHistory = (arr) =>
    //     localStorage.setItem(storageKey, JSON.stringify(arr));

    // // load history when user (key) changes
    // useEffect(() => { setHistory(readSearchHistory()); }, [storageKey]);

    // const saveHistory = (arr) => { setHistory(arr); writeSearchHistory(arr); };

    const handleSearch = async (term) => {
        const searchTerm = (term ?? q).trim();
        if (!searchTerm) return;

        // Clear any previous errors
        setError(null);

        // Save to history FIRST
        // const updatedHistory = [searchTerm, ...history.filter(x => x !== searchTerm)].slice(0, 10);
        // saveHistory(updatedHistory);

        try {
            console.log('Starting search for:', searchTerm);

            const response = await api.get(`/api/tariffs/search`, {
                params: {
                    keyword: encodeURIComponent(q)
                }
            });

            console.log(response);
            console.log(response.data);
            setResults(response.data);
            navigate("/results", { state: { results: response.data, keyword: q } });
        } catch (error) {
            console.error('Error during search:', error);
            setError('Search failed. Please check your connection and try again.');
            setResults([]); // Clear results on error
        }
    };

    // Optional live search (debounced)
    // const debouncedSearch = useMemo(() => (value) => {
    //     if (timerRef.current) clearTimeout(timerRef.current);
    //     timerRef.current = setTimeout(() => {
    //         const v = value.trim();
    //         if (v && live) {
    //             handleSearch(v); // Use the same unified search function
    //         }
    //     }, 300);
    // }, [live, history]); // Added history as dependency

    // const removeSearchTerm = (term) =>
    //     saveSearchHistory(history.filter(x => x !== term));

    // const clearSearchHistory = () => saveSearchHistory([]);


    // show loading indicator if isLoading is true
    // const loadingIndicator = isLoading && <div className="loading">Loading...</div>;

    // optional live search (off by default)
    // const debounced = useMemo(() => (value) => {
    //     if (timerRef.current) clearTimeout(timerRef.current);
    //     timerRef.current = setTimeout(() => {
    //         const v = value.trim();
    //         if (v && live && onSearch) onSearch(v);
    //     }, 300);
    // }, [live, onSearch]);

    // const removeOne = (term) => saveHistory(history.filter(x => x !== term));
    // const clearAll = () => saveHistory([]);

    return (
        <section className="search-section">
            <div
                className="search-bar"
                role="search"
                aria-label="Site search"
                onClick={() => inputRef.current?.focus()}
            >
                <input
                    ref={inputRef}
                    className="search-input"
                    value={q}
                    // onChange={(e) => { setQ(e.target.value); debounced(e.target.value); }}
                    onChange={(e) => { setQ(e.target.value); }}
                    onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                    placeholder="Search…"
                    aria-label="Search input"
                />
                <button type="button" className="btn-primary search-btn" onClick={() => handleSearch()}>
                    Search
                </button>
            </div>

            {/* Show error if it exists */}
            {error && (
                <div className="search-error" style={{ color: 'red', padding: '10px' }}>
                    {error}
                </div>
            )}

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
        </section>
    );
}