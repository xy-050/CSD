import React from 'react';
import NavBar from './NavBar.jsx';

export default function SearchResults({ results, onSelectOption, user, setUser, setCurrentPage }) {
  // Debug: Log the props to see what's being passed
  console.log("SearchResults component rendered");
  console.log("SearchResults user prop:", user);
  console.log("SearchResults results:", results);

    if (!results || results.length === 0) {
    return (
      <>
        <NavBar user={user} setUser={setUser} setCurrentPage={setCurrentPage} />
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
      <NavBar user={user} setUser={setUser} setCurrentPage={setCurrentPage} />
      <div className="search-results">
        <h2>Search Results</h2>
        <div className="results-grid">
          {results.map((result, index) => (
            <div className="result-item" key={index}>
              <h3>{result.htsno}</h3>
              <p>{result.description}</p>
              <button onClick={() => onSelectOption(result)}>Shortlist</button>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}