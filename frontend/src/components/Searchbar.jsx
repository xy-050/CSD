import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ErrorMessage } from "../components/ErrorMessage";

const CATEGORIES = [
    { key: 'Sugar', label: 'Sugar', icon: '🍬' },
    { key: 'Bread', label: 'Bread', icon: '🍞' },
    { key: 'Milk', label: 'Milk', icon: '🥛' },
    { key: 'Egg', label: 'Egg', icon: '🥚', dataTour: 'category-egg' },
    { key: 'Rice', label: 'Rice', icon: '🍚' },
];

export default function SearchBar() {
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleSearch = (searchTerm) => {
        const term = searchTerm.trim();

        // Navigate to results page with search term
        navigate("/results", {
            state: {
                keyword: term,
                results: term ? undefined : []
            }
        });
    };

    return (
        <section className="search-section">
            <div className="search-bar" role="search" aria-label="Site search">
                <div className="category-buttons" data-tour="category-buttons">
                    {CATEGORIES.map(cat => (
                        <button
                            key={cat.key}
                            type="button"
                            className="category-btn"
                            onClick={() => handleSearch(cat.key)}
                            aria-label={`Search ${cat.label}`}
                            {...(cat.dataTour && { 'data-tour': cat.dataTour })}
                        >
                            <span className="category-icon">{cat.icon}</span>
                            <span className="category-label">{cat.label}</span>
                        </button>
                    ))}
                </div>
            </div>

            <ErrorMessage message={error} />
        </section>
    );
}
