import { useEffect, useMemo, useRef, useState } from "react";

export default function SearchBar({ onSearch, user, live = false }) {
  const [q, setQ] = useState("");
  const [history, setHistory] = useState([]);
  const inputRef = useRef(null);
  const timerRef = useRef(null);

  // per-user storage key
  const storageKey = useMemo(
    () => `history:${user?.email || "anon"}`,
    [user?.email]
  );

  // safe JSON
  const readStore = () => {
    try { return JSON.parse(localStorage.getItem(storageKey) || "[]"); }
    catch { return []; }
  };
  const writeStore = (arr) =>
    localStorage.setItem(storageKey, JSON.stringify(arr));

  // load history when user (key) changes
  useEffect(() => { setHistory(readStore()); }, [storageKey]);

  const saveHistory = (arr) => { setHistory(arr); writeStore(arr); };

  const submit = (term) => {
    const t = (term ?? q).trim();
    if (!t) return;
    const next = [t, ...history.filter(x => x !== t)].slice(0, 10);
    saveHistory(next);
    if (onSearch) onSearch(t);
  };

  // optional live search (off by default)
  const debounced = useMemo(() => (value) => {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      const v = value.trim();
      if (v && live && onSearch) onSearch(v);
    }, 300);
  }, [live, onSearch]);

  const removeOne = (term) => saveHistory(history.filter(x => x !== term));
  const clearAll  = () => saveHistory([]);

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
          onKeyDown={(e) => e.key === "Enter" && submit()}
          placeholder="Search…"
          aria-label="Search input"
        />
        <button type="button" className="btn-primary search-btn" onClick={() => submit()}>
          Search
        </button>
      </div>

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
                onClick={() => submit(term)}
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