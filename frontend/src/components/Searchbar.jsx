import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function SearchBar({ }) {
    const [q, setQ] = useState("");
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    // add category definitions
    const categories = [
        { key: 'Sugar', label: 'Sugar', icon: '🍬', dataTour: null},
        { key: 'Bread', label: 'Bread', icon: '🍞', dataTour: null},
        { key: 'Milk', label: 'Milk', icon: '🥛', dataTour: null},
        { key: 'Egg', label: 'Egg', icon: '🥚', dataTour: 'category-egg'},
        { key: 'Rice', label: 'Rice', icon: '🍚', dataTour: null},
    ];

    const handleSearch = (term) => {
        const searchTerm = (term ?? q).trim();

        // If no search term, go directly to the results page with empty results
        if (!searchTerm) {
            navigate("/results", { state: { results: [], keyword: "" } });
            return;
        }

        // Navigate directly to results page with the search term
        navigate("/results", { state: { keyword: searchTerm } });
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
        </section>
    );
}