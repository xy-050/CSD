import { useLocation, useNavigate } from 'react-router-dom';
import { useTour } from '../components/TourContext.jsx';
import NavBar from '../components/NavBar.jsx';
import Sidebar from '../components/Sidebar.jsx';
import { SearchResultsHeader } from '../components/SearchResultsHeader.jsx';
import { ResultCard } from '../components/ResultCard.jsx';
import { EmptyResults } from '../components/EmptyResults.jsx';
import { LoadingResults } from '../components/LoadingResults.jsx';
import { useResponsiveSidebar } from '../hooks/useResponsiveSidebar.jsx';
import { useSearchResults } from '../hooks/useSearchResults.jsx';
import { useSaveQuery } from '../hooks/useSaveQuery.jsx';
import { useTourSpotlight } from '../hooks/useTourSpotlight.jsx';
import api from '../api/AxiosConfig.jsx';

export default function SearchResults() {
    const { tourState, setCurrentStep } = useTour();
    const location = useLocation();
    const navigate = useNavigate();

    // Extract location state
    const { keyword, results: navigationResults, originalCategory } = location.state || {};

    // Custom hooks
    const { isOpen: sidebarOpen, toggle: toggleSidebar, closeOnMobile: closeSidebar } = useResponsiveSidebar();
    const { results, loading, error } = useSearchResults(keyword, navigationResults);
    const saveQuery = useSaveQuery();

    // Display only first 8 results
    const displayedResults = Array.isArray(results) ? results.slice(0, 8) : [];

    // Tour spotlight logic
    const { parentIndex, spotlightIndex } = useTourSpotlight(displayedResults, tourState.isActive);

    // Get tour data attribute for a result
    const getTourDataAttr = (index) => {
        if (!tourState.isActive) return undefined;
        if (index === parentIndex) return 'result-parent';
        if (index === spotlightIndex) return 'result-item';
        return undefined;
    };

    // Handle clicking on a result
    const handleResultAction = async (result) => {
        const hasGeneralTariff = result.general && result.general.trim() !== '';

        if (hasGeneralTariff) {
            // Navigate to calculator
            await handleCalculateResult(result);
        } else {
            // Drill down into subcategories
            await handleDrillDown(result);
        }
    };

    // Navigate to calculator with result
    const handleCalculateResult = async (result) => {
        // Save query for analytics
        if (result.htsno) {
            await saveQuery(result.htsno);
        }

        const formattedResult = {
            ...result,
            description: result.descriptionChain?.length
                ? result.descriptionChain[result.descriptionChain.length - 1]
                : 'No description available',
            fullDescriptionChain: result.descriptionChain,
        };

        navigate("/calculator", {
            state: {
                result: formattedResult,
                keyword: originalCategory || keyword
            },
        });
    };

    // Drill down into subcategories
    const handleDrillDown = async (result) => {
        try {
            const response = await api.get(`/product/category/${result.htsno}`);
            console.log('Subcategories response:', response.data);

            const subcategories = response.data.categories || [];

            // Filter and format subcategories
            const formattedResults = Array.from(subcategories)
                .filter(product => product.general && product.general.trim() !== "")
                .map(product => ({
                    htsno: product.htsCode,
                    descriptionChain: [product.description || product.htsCode],
                    general: product.general,
                    category: product.category
                }));

            console.log('Formatted subcategory results:', formattedResults);

            // Navigate to new results page
            navigate("/results", {
                state: {
                    keyword: result.htsno,
                    results: formattedResults,
                    originalCategory: originalCategory || keyword
                },
                replace: true
            });

            // Advance tour after "See More" click
            if (tourState.isActive) {
                setTimeout(() => {
                    setCurrentStep(prev => prev + 1);
                }, 100);
            }
        } catch (error) {
            console.error('Error fetching subcategories:', error);
            alert('Failed to load subcategories. Please try again.');
        }
    };

    // Loading state
    if (loading) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content" onClick={closeSidebar}>
                        <LoadingResults />
                    </main>
                </div>
            </div>
        );
    }

    // Empty state
    if (displayedResults.length === 0) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content" onClick={closeSidebar}>
                        <EmptyResults />
                    </main>
                </div>
            </div>
        );
    }

    // Normal results render
    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />

                <main className="main-content" onClick={closeSidebar}>
                    <div className="search-results">
                        <SearchResultsHeader
                            keyword={keyword}
                            originalCategory={originalCategory}
                        />

                        <div className="results-grid">
                            {displayedResults.map((result, index) => (
                                <ResultCard
                                    key={`${result.htsno}-${index}`}
                                    result={result}
                                    index={index}
                                    tourDataAttr={getTourDataAttr(index)}
                                    onAction={handleResultAction}
                                />
                            ))}
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
}
