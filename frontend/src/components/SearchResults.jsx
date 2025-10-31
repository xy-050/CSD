import { useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import NavBar from './NavBar.jsx';
import Sidebar from './Sidebar.jsx';
import api from '../api/AxiosConfig.jsx';

export default function SearchResults() {
  const location = useLocation();
  const { results, keyword } = location.state || {};
  const navigate = useNavigate();

  // sidebar
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const toggleSidebar = () => setSidebarOpen(v => !v);
  const closeSidebar = () => setSidebarOpen(false);

  const handleShortlist = async (result) => {
    if (result.general && result.general.trim() !== '') {
      const formattedResult = {
        ...result,
        description:
          result.descriptionChain && result.descriptionChain.length > 0
            ? result.descriptionChain[result.descriptionChain.length - 1]
            : 'No description available',
        fullDescriptionChain: result.descriptionChain
      };
      navigate('/calculator', { state: { result: formattedResult, keyword } });
    } else {
      try {
        const response = await api.get('/api/tariffs/search', {
          params: { keyword: result.htsno }
        });
        navigate('/results', {
          state: { results: response.data, keyword },
          replace: true
        });
      } catch (error) {
        console.error('Error searching HTS subcategories:', error);
        alert('Failed to load subcategories. Please try again.');
      }
    }
  };

  if (!results || results.length === 0) {
    return (
      <div className="homepage">
        <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
        <div className="homepage-container">
          <Sidebar isOpen={sidebarOpen} />
          <main className="main-content" onClick={closeSidebar}>
            <div className="search-results">
              <div className="no-results-container">
                <div className="no-results-icon">🔍</div>
                <h2 className="no-results-title">No Results Found</h2>
                <p className="no-results-message">
                  We couldn't find any matches for your search. Try adjusting your filters or search terms.
                </p>
              </div>
            </div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="homepage">
      <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
      <div className="homepage-container">
        <Sidebar isOpen={sidebarOpen} />
        <main className="main-content" onClick={closeSidebar}>
          <div className="search-results">
            <h2>Search Results{keyword ? ` for "${keyword}"` : ''}</h2>
            <div className="results-grid">
              {results.map((result, index) => (
                <div className="result-item" key={index}>
                  <h3>{result.htsno}</h3>
                  <div className="description-chain">
                    {result.descriptionChain?.length ? (
                      result.descriptionChain.map((desc, i) => (
                        <p key={i} className="description-level">
                          {'→ '.repeat(i)}{desc}
                        </p>
                      ))
                    ) : (
                      <p>No description available</p>
                    )}
                  </div>
                  {result.general && (
                    <p className="tariff-info"><strong>General Tariff:</strong> {result.general}</p>
                  )}
                  {result.units && (
                    <p className="units-info"><strong>Units:</strong> {result.units}</p>
                  )}
                  <button onClick={() => handleShortlist(result)}>
                    {result.general?.trim() ? 'Calculate' : 'See More'}
                  </button>
                </div>
              ))}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
