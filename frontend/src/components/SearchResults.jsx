import { useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useTour } from './Tour/TourContext.jsx';
import NavBar from './NavBar.jsx';
import Sidebar from './Sidebar.jsx';
import api from '../api/AxiosConfig.jsx';

export default function SearchResults() {
    const { tourState } = useTour();
    const location = useLocation();
    const { keyword } = location.state || {};
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
        }
        getUserDetails();
    }, []);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch results when keyword changes or when receiving new results from navigation
    const [results, setResults] = useState(null);

    useEffect(() => {
        const fetchResults = async () => {
            console.log("location state:", location.state);

            // If we have results from navigation state, use those
            if (location.state?.results) {
                setResults(location.state.results);
                setLoading(false);
                return;
            }

            // Otherwise fetch new results
            if (!keyword) {
                setResults([]);
                setLoading(false);
                return;
            }

            try {
                const response = await api.get(`/product/category/search/${encodeURIComponent(keyword)}`);
                if (response.data.categories) {
                    console.log(response.data);
                    // Transform the categories into a format matching our results structure
                    const formattedResults = Array.from(response.data.categories).map(product => ({
                        htsno: product.htsCode || product,
                        descriptionChain: [product.description || product],
                        general: product.general || "",
                        category: product.category,
                        special: product.special || "",
                        units: product.units || ""
                    }));
                    console.log("formattedResults =", formattedResults);
                    setResults(formattedResults);
                } else {
                    setResults([]);
                }
            } catch (err) {
                console.error('Error fetching results:', err);
                setError('Failed to fetch results. Please try again.');
                setResults([]);
            } finally {
                setLoading(false);
            }
        };

        setLoading(true);
        fetchResults();
    }, [keyword]);

    // Category icons
    const categoryIcons = {
        sugar: '🍬',
        bread: '🍞',
        milk: '🥛',
        egg: '🥚',
        rice: '🍚',
    };
    const currentCategoryIcon = location.state?.originalCategory
        ? categoryIcons[String(location.state.originalCategory).toLowerCase()]
        : keyword
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
                const response = await api.get(`/product/category/${result.htsno}`);
                console.log('Subcategories response:', response.data);

                // Transform the subcategories data similar to the initial search
                const subcategories = response.data.categories || [];
                const formattedResults = Array.from(subcategories)
                    .filter(product => product.general && product.general.trim() !== "")
                    .map(product => ({
                        htsno: product.htsCode,
                        descriptionChain: [product.description || product.htsCode],
                        general: product.general,
                        category: product.category
                    }));
                console.log('Formatted subcategory results:', formattedResults);
                console.log('Subcategories data:', subcategories);

                // Navigate to a fresh instance of the results page
                navigate("/results", {
                    state: {
                        keyword: result.htsno,  // Use the HTS code as the new keyword
                        results: formattedResults,
                        originalCategory: keyword  // Keep track of the original category for the icon
                    },
                    replace: true  // Replace current history entry
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

    // Handle loading state
    if (loading) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content" onClick={closeSidebar}>
                        <div className="search-results">
                            <div className="loading-container">
                                <div className="loading-icon">⌛</div>
                                <h2 className="loading-title">Loading Results...</h2>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        );
    }

    // Handle no products to show
    if (displayedResults.length == 0) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content" onClick={closeSidebar}>
                        <div className="search-results">
                            <div className="loading-container">
                                <h2 className="loading-title">🔎Nothing to Show</h2>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        )
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
                            data-tour="search-results-header"
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
                                <div
                                    className="result-item"
                                    key={index}
                                    // Add data attribute to the first item with a general tariff for tour targeting
                                    data-tour={
                                        tourState.isActive &&
                                            index === displayedResults.findIndex(r => r.general && r.general.trim() !== '')
                                            ? 'result-item'
                                            : undefined
                                    }
                                >
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