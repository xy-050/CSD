import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import NavBar from './NavBar.jsx';

export default function SearchResults({ }) {
    // // Debug: Log the props to see what's being passed
    // console.log("SearchResults component rendered");
    // console.log("SearchResults user prop:", user);
    // console.log("SearchResults results:", results);

    const location = useLocation();
    const { results, keyword } = location.state || {};
    const navigate = useNavigate();
    
    const handleShortlist = async (result) => {
        navigate("/calculator", { state: { result: result, keyword: keyword } })
    }

    if (!results || results.length === 0) {
        return (
            <>
                <NavBar />
                <div className="search-results">
                    <div className="no-results-container">
                        <div className="no-results-icon">🔍</div>
                        <h2 className="no-results-title">No Results Found</h2>
                        <p className="no-results-message">
                            We couldn't find any matches for your search. Try adjusting your filters or search terms.
                        </p>
                    </div>
                </div>
            </>
        );
    }

    return (
        <>
            <NavBar />
            <div className="search-results">
                <h2>Search Results</h2>
                <div className="results-grid">
                    {results.map((result, index) => (
                        <div className="result-item" key={index}>
                            <h3>{result.htsno}</h3>
                            <p>{result.description}</p>
                            <button onClick={() => handleShortlist(result)}>Shortlist</button>
                        </div>
                    ))}
                </div>
            </div>
        </>
    );
}