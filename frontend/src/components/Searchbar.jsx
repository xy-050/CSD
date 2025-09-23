import { useEffect, useMemo, useRef, useState } from "react";

export default function SearchBar({ onSearch, user, live = false, setCurrentPage }) {
  const [q, setQ] = useState("");
  const [history, setHistory] = useState([]);
  const [results, setResults] = useState([]);
  const [error, setError] = useState(null);
  const inputRef = useRef(null);
  const timerRef = useRef(null);

  // API configuration
  const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  // per-user storage key
  const storageKey = useMemo(
    () => `history:${user?.email || "anon"}`,
    [user?.email]
  );

  // safe JSON
  const readSearchHistory = () => {
    try { return JSON.parse(localStorage.getItem(storageKey) || "[]"); }
    catch { return []; }
  };
  const writeSearchHistory = (arr) =>
    localStorage.setItem(storageKey, JSON.stringify(arr));

  // load history when user (key) changes
  useEffect(() => { setHistory(readSearchHistory()); }, [storageKey]);

  const saveHistory = (arr) => { setHistory(arr); writeSearchHistory(arr); };

  // COMBINED search function that handles both history AND API call
  const handleSearch = async (term) => {
    const searchTerm = (term ?? q).trim();
    if (!searchTerm) return;

    // Clear any previous errors
    setError(null);

    // Save to history FIRST
    const updatedHistory = [searchTerm, ...history.filter(x => x !== searchTerm)].slice(0, 10);
    saveHistory(updatedHistory);
    // const submit = (term) => {
    //   const t = (term ?? q).trim();
    //   if (!t) return;
    //   const next = [t, ...history.filter(x => x !== t)].slice(0, 10);
    //   saveHistory(next);
    //   if (onSearch) onSearch(t);
    // };

    // API call function
    // const runSearch = async (term) => {
    //   console.log('Starting search for:', term); // Add this line
    //   try {
    //     const url = `http://localhost:8080/api/tariffs/search?keyword=${term}`;
    //     console.log('Making request to:', url);

    //     const response = await fetch(
    //       `http://localhost:8080/api/tariffs/search?keyword=${term}`, {
    //       method: 'GET',
    //       headers: {
    //         'Content-Type': 'application/json'
    //       }
    //     }
    //     );

    //     console.log('Response status:', response.status);
    //     console.log('Response ok:', response.ok);

    //     if (!response.ok) {
    //       throw new Error('Network response was not ok');
    //     }
    //     const data = await response.json();
    //     console.log('API returned data:', data);
    //     setResults(data);
    //     setCurrentPage("searchResults"); // Navigate to search results page
    //   } catch (error) {
    //     console.error('Error during search:', error);
    //     setResults([]); // Optionally clear results on error
    //   }
    // };

    try {
      console.log('Starting search for:', searchTerm);

      const url = `${API_BASE_URL}/api/tariffs/search?keyword=${encodeURIComponent(searchTerm)}`;
      console.log('Making request to:', url);

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('Response status:', response.status);

      if (!response.ok) {
        throw new Error(`Search failed: ${response.status}`);
      }

      const data = await response.json();
      console.log('API returned data:', data);

      // Update results state
      setResults(data);

      // Navigate to search results page
      setCurrentPage("searchResults");

      // Call the parent's onSearch if it exists (for backwards compatibility)
      if (onSearch) onSearch(searchTerm);

    } catch (error) {
      console.error('Error during search:', error);
      setError('Search failed. Please check your connection and try again.');
      setResults([]); // Clear results on error
    } finally {
      // setIsLoading(false);
    }
  };

  // Optional live search (debounced)
  const debouncedSearch = useMemo(() => (value) => {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      const v = value.trim();
      if (v && live) {
        handleSearch(v); // Use the same unified search function
      }
    }, 300);
  }, [live, history]); // Added history as dependency

  const removeSearchTerm = (term) =>
    saveSearchHistory(history.filter(x => x !== term));

  const clearSearchHistory = () => saveSearchHistory([]);


  // show loading indicator if isLoading is true
  // const loadingIndicator = isLoading && <div className="loading">Loading...</div>;

  // optional live search (off by default)
  const debounced = useMemo(() => (value) => {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      const v = value.trim();
      if (v && live && onSearch) onSearch(v);
    }, 300);
  }, [live, onSearch]);

  const removeOne = (term) => saveHistory(history.filter(x => x !== term));
  const clearAll = () => saveHistory([]);

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
          onChange={(e) => { setQ(e.target.value); debounced(e.target.value); }}
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