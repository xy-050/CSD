import { useMemo, useRef, useState } from "react"

function SearchBar({ onSearch }) {
  const [q, setQ] = useState("")
  const [history, setHistory] = useState([])
  const inputRef = useRef(null)
  const timerRef = useRef(null)

  const submit = (term) => {
    const t = (term ?? q).trim()
    if (!t) return
    setHistory(prev => [t, ...prev.filter(x => x !== t)].slice(0, 10))
    if (onSearch) onSearch(t)
  }

  const debounced = useMemo(() => {
    return (value) => {
      if (timerRef.current) clearTimeout(timerRef.current)
      timerRef.current = setTimeout(() => {
        const v = value.trim()
        if (v && onSearch) onSearch(v)
      }, 300)
    }
  }, [onSearch])

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
          style={{ cursor: "text" }}
          value={q}
          onChange={(e) => { setQ(e.target.value); debounced(e.target.value) }}
          onKeyDown={(e) => e.key === "Enter" && submit()}
          placeholder="Search…"
          aria-label="Search input"
        />
        <button
          type="button"
          className="btn-primary search-btn"
          onClick={() => submit()}
        >
          Search
        </button>
      </div>

      {!!history.length && (
        <div className="search-history">
          <div className="history-header">
            <span>Recent searches</span>
            <button className="link-btn" onClick={() => setHistory([])}>Clear all</button>
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
                  onClick={(e) => {
                    e.stopPropagation()
                    setHistory(prev => prev.filter(x => x !== term))
                  }}
                >
                  ×
                </span>
              </button>
            ))}
          </div>
        </div>
      )}
    </section>
  )
}

export default SearchBar