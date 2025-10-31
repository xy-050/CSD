import { useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import NavBar from './NavBar.jsx';
import Sidebar from './Sidebar.jsx';
import api from '../api/AxiosConfig.jsx';

export default function SearchResults() {
  const location = useLocation();
  const { results, keyword } = location.state || {};
  const navigate = useNavigate();

  // Sidebar state
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const toggleSidebar = () => setSidebarOpen(v => !v);
  const closeSidebar = () => setSidebarOpen(false);

  // User state
  const [currentUserID, setCurrentUserID] = useState(null);

  // Fetch user details on mount
  useEffect(() => {
    const getUserDetails = async () => {
      try {
        const response = await api.get("/currentUserDetails");
        setCurrentUserID(response.data.userId);
      } catch (error) {
        console.log(error);
      }
    };
    getUserDetails();
  }, []);

  // Category icons
  const categoryIcons = {
    sugar: '🍬',
    bread: '🍞',
    milk: '🥛',
    egg: '🥚',
    rice: '🍚',
  };
  const currentCategoryIcon = keyword
    ? categoryIcons[String(keyword).toLowerCase()]
    : null;

  // Helper to save query
  const saveQuery = async (htsCode) => {
    if (!currentUserID) return;
    try {
      const queryData = {
        userID: { userID: currentUserID },
        htsCode: htsCode,
        originCountry: null,
        modeOfTransport: null,
        quantity: 0,
      };
      await api.post('/api/tariffs/queries', queryData);
    } catch (error) {
      console.error('Failed to save query:', error);
    }
  };

  // When user clicks a result
  const handleShortlist = async (result) => {
    if (result.general && result.general.trim() !== '') {
      if (result.htsno) await saveQuery(result.htsno);

      const formattedResult = {
        ...result,
        description:
          result.descriptionChain?.length
            ? result.descriptionChain[result.descriptionChain.length - 1]
            : 'No description available',
        fullDescriptionChain: result.descriptionChain,
      };
      navigate("/calculator", {
        state: { result: formattedResult, keyword },
      });
    } else {
      try {
        const response = await api.get(`/api/tariffs/search`, {
          params: { keyword: result.htsno },
        });
        navigate("/results", {
          state: { results: response.data, keyword },
        });
      } catch (error) {
        console.error('Error searching HTS subcategories:', error);
        alert('Failed to load subcategories. Please try again.');
      }
    }
  };

  // Show only first 8 results
  const displayedResults = Array.isArray(results)
    ? results.slice(0, 8)
    : [];

  // Handle empty results
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

  // ✅ Normal results render
  return (
    <div className="homepage">
      <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
      <div className="homepage-container">
        <Sidebar isOpen={sidebarOpen} />
        <main className="main-content" onClick={closeSidebar}>
          <div className="search-results">
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                marginBottom: '0.75rem',
              }}
            >
              {currentCategoryIcon && (
                <span
                  style={{ fontSize: '2rem', lineHeight: 1 }}
                  aria-hidden="true"
                >
                  {currentCategoryIcon}
                </span>
              )}
              <h2 style={{ margin: 0 }}>
                Search Results{keyword ? ` — ${keyword}` : ''}
              </h2>
            </div>

            <div className="results-grid">
              {displayedResults.map((result, index) => (
                <div className="result-item" key={index}>
                  <h3>{result.htsno}</h3>
                  <div className="description-chain">
                    {result.descriptionChain?.length ? (
                      result.descriptionChain.map((desc, i) => (
                        <p key={i} className="description-level">
                          {'→ '.repeat(i)}
                          {desc}
                        </p>
                      ))
                    ) : (
                      <p>No description available</p>
                    )}
                  </div>
                  {result.general && (
                    <p className="tariff-info">
                      <strong>General Tariff:</strong> {result.general}
                    </p>
                  )}
                  {result.units && (
                    <p className="units-info">
                      <strong>Units:</strong> {result.units}
                    </p>
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
