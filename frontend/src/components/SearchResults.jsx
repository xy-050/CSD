import React from 'react';

const SearchResults = ({ results, onSelectOption }) => {
  if (!results || results.length === 0) {
    return (
      <div className="no-results-wrapper">
        <p className="no-results">No results found. Please refine your search.</p>
      </div>
    );
  }

  return (
    <div className="search-results">
      <h2>Search Results</h2>
      <div className="results-grid">
        {results.map((result, index) => (
          <div className="result-item" key={index}>
            <h3>{result.name}</h3>
            <p>{result.description}</p>
            <button onClick={() => onSelectOption(result)}>Shortlist</button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SearchResults;