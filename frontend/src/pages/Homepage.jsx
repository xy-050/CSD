import { useNavigate } from "react-router-dom";

import { useTour } from "../components/TourContext.jsx";
import NavBar from "../components/NavBar.jsx";
import SearchBar from "../components/Searchbar.jsx";
import Sidebar from "../components/Sidebar.jsx";
import { FeatureCard } from "../components/FeatureCard";
import { TopProductsSection } from "../components/TopProductsSection/TopProductsSection.jsx";
import { TourButton } from "../components/TourButton/TourButton.jsx";

import { useTypingEffect } from "../hooks/useTypingEffect";
import { useResponsiveSidebar } from "../hooks/useResponsiveSidebar";
import { useTopProducts } from "../hooks/useTopProducts";
import { useSaveQuery } from "../hooks/useSaveQuery";
import api from "../api/AxiosConfig.jsx";

export default function HomePage() {
    const { startTour } = useTour();
    const navigate = useNavigate();

    // Custom hooks
    const { typedText, isDone } = useTypingEffect("Welcome to your dashboard :)");
    const { isOpen: sidebarOpen, toggle: toggleSidebar, closeOnMobile: closeSidebar } = useResponsiveSidebar();
    const { products: topProducts, loading: topLoading, error: topError } = useTopProducts(10);
    const saveQuery = useSaveQuery();

    // Handle tour start button click
    const handleTourStart = () => {
        // Close sidebar if on mobile
        if (window.innerWidth <= 768) {
            closeSidebar();
        }
        // Small delay to let sidebar close
        setTimeout(() => {
            startTour();
        }, 300);
    };

    // Navigate to product calculator
    const handleProductClick = async (product) => {
        console.log('Clicking on product:', product);

        try {
            // Fetch product details
            const response = await api.get(`/product/hts/${encodeURIComponent(product.htsCode)}`);
            console.log('Response received:', response.data);

            if (!response.data || !response.data.htsCode) {
                console.error('Product not found in response:', response.data);
                alert('Product details not found');
                return;
            }

            // Save query for analytics
            await saveQuery(product.htsCode);

            // Format result for calculator
            const formattedResult = {
                htsno: product.htsCode,
                description: product.description || 'No description available',
                descriptionChain: [product.description || 'No description available'],
                fullDescriptionChain: [product.description || 'No description available'],
                category: product.category || 'Unknown',
            };

            console.log('Navigating to calculator with:', formattedResult);

            // Navigate to calculator
            navigate('/calculator', {
                state: { result: formattedResult, keyword: product.htsCode }
            });
        } catch (error) {
            console.error('Error navigating to product details:', error);
            alert(`Failed to navigate to product details. ${error.message}`);
        }
    };

    // Feature cards configuration
    const features = [
        {
            icon: '📈',
            iconColor: 'blue',
            title: 'Insights',
            description: 'Track activity and recent events at a glance.',
            buttonText: 'View reports',
            onClick: () => navigate('/home')
        },
        {
            icon: '⭐️',
            iconColor: 'green',
            title: 'Favourites',
            description: 'Organize work with a soothing, minimal UI.',
            buttonText: 'Open favourites',
            onClick: () => navigate('/favourites')
        },
        {
            icon: '⚙️',
            iconColor: 'purple',
            title: 'Settings',
            description: 'Tune preferences and notification rules.',
            buttonText: 'Manage',
            onClick: () => navigate('/profile')
        }
    ];

    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />

            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />

                <main className="main-content" onClick={closeSidebar}>
                    {/* Hero Section */}
                    <section className="hero-section">
                        <h1 className="hero-title" data-tour="hero-title">
                            {typedText}
                            {!isDone && <span className="caret" aria-hidden="true" />}
                        </h1>

                        <TourButton onClick={handleTourStart} />

                        <SearchBar />
                    </section>

                    {/* Feature Cards */}
                    <section className="features-grid">
                        {features.map((feature, index) => (
                            <FeatureCard key={index} {...feature} />
                        ))}
                    </section>

                    {/* Top Products */}
                    <TopProductsSection
                        products={topProducts}
                        loading={topLoading}
                        error={topError}
                        onProductClick={handleProductClick}
                    />
                </main>
            </div>
        </div>
    );
}
