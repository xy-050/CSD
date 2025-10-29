import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import NavBar from './NavBar.jsx';
import api from '../api/AxiosConfig.jsx';

export default function SearchResults({ }) {
    // // Debug: Log the props to see what's being passed
    // console.log("SearchResults component rendered");
    // console.log("SearchResults user prop:", user);
    // console.log("SearchResults results:", results);

    const location = useLocation();
    const { results, keyword } = location.state || {};
    const navigate = useNavigate();
    const [currentUserID, setCurrentUserID] = useState(null);

    useEffect(() => {
        const getUserDetails = async () => {
            try {
                const response = await api.get("/currentUserDetails");
                console.log(response);
                setCurrentUserID(response.data.userId);
            } catch (error) {
                console.log(error);
            }
        }
        getUserDetails();
    }, []);
    
    // map category keys to icons (keep in sync with SearchBar)
    const categoryIcons = {
        sugar: '🍬',
        bread: '🍞',
        milk: '🥛',
        egg: '🥚',
        rice: '🍚',
    };

    // determine icon for current keyword (if any) - case-insensitive
    const currentCategoryIcon = keyword ? categoryIcons[String(keyword).toLowerCase()] : null;
    
    // show at most 8 results
    const displayedResults = Array.isArray(results) ? results.slice(0, 8) : [];
    
    // Helper function to save query to database
    const saveQuery = async (htsCode) => {
        if (!currentUserID) {
            console.warn('Cannot save query: user ID not available');
            return;
        }

        try {
            const queryData = {
                userID: { userID: currentUserID }, // Account object reference
                htsCode: htsCode,
                originCountry: null,
                modeOfTransport: null,
                quantity: 0
            };

            console.log('Saving query:', queryData);
            await api.post('/api/tariffs/queries', queryData);
            console.log('Query saved successfully for HTS code:', htsCode);
        } catch (error) {
            console.error('Failed to save query:', error);
            // Don't block user flow if save fails - just log it
        }
    };

    const handleShortlist = async (result) => {
        // If general tariff exists, go to calculator
        if (result.general && result.general.trim() !== '') {
            // ONLY save query when going to calculator (not for "See More")
            if (result.htsno) {
                await saveQuery(result.htsno);
            }

            const formattedResult = {
                ...result,
                // Create a single description from the description chain for backward compatibility
                description: result.descriptionChain && result.descriptionChain.length > 0 
                    ? result.descriptionChain[result.descriptionChain.length - 1] // Use the most specific description
                    : 'No description available',
                // Keep the full chain for detailed display if needed
                fullDescriptionChain: result.descriptionChain
            };
            
            console.log('Going to calculator with result:', formattedResult);
            navigate("/calculator", { 
                state: { 
                    result: formattedResult, 
                    keyword: keyword 
                } 
            });
        } else {
            // No general tariff, search for subcategories
            try {
                console.log('Searching for HTS code subcategories:', result.htsno);
                
                const response = await api.get(`/api/tariffs/search`, {
                    params: {
                        keyword: result.htsno
                    }
                });
                
                console.log('HTS subcategory results:', response.data);
                // Navigate to the same page with new results (do NOT replace history)
                navigate("/results", { 
                    state: { 
                        results: response.data, 
                        keyword: keyword
                    }
                    // removed replace: true
                });
            } catch (error) {
                console.error('Error searching HTS subcategories:', error);
                alert('Failed to load subcategories. Please try again.');
            }
        }
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
                {/* header now shows selected category icon when available */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
                    {currentCategoryIcon && (
                        <span style={{ fontSize: '2rem', lineHeight: 1 }} aria-hidden="true">
                            {currentCategoryIcon}
                        </span>
                    )}
                    <h2 style={{ margin: 0 }}>Search Results{keyword ? ` — ${keyword}` : ''}</h2>
                </div>

                <div className="results-grid">
                    {displayedResults.map((result, index) => (
                        <div className="result-item" key={index}>
                            <h3>{result.htsno}</h3>
                            <div className="description-chain">
                                {result.descriptionChain && result.descriptionChain.length > 0 ? (
                                    result.descriptionChain.map((desc, descIndex) => (
                                        <p key={descIndex} className="description-level">
                                            {"→ ".repeat(descIndex)}{desc}
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
                                {result.general && result.general.trim() !== '' ? 'Calculate' : 'See More'}
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </>
    );
}