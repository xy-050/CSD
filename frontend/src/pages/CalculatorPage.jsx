import { useState, useEffect } from "react";
import { useLocation } from 'react-router-dom';
import NavBar from "../components/NavBar";
import Sidebar from "../components/Sidebar";
import { CalculatorInputs } from '../components/CalculatorInputs';
import { CalculationResults } from '../components/CalculationResults';
import { DataVisualizationsSection } from '../components/DataVisualizationsSection';
import { EmptyCalculator } from '../components/EmptyCalculator';
import { useResponsiveSidebar } from '../hooks/useResponsiveSidebar';
import { useCurrentUser } from '../hooks/useCurrentUser';
import { useFavorite } from '../hooks/useFavorite';
import { useCountryTariffs } from '../hooks/useCountryTariffs';
import { useTariffCalculation } from '../hooks/useTariffCalculation';

export default function CalculatorPage() {
    const location = useLocation();
    const { result, keyword } = location.state || {};

    // Sidebar management
    const {
        isOpen: sidebarOpen,
        toggle: toggleSidebar,
        closeOnMobile: closeSidebar
    } = useResponsiveSidebar();

    // User management
    const { user } = useCurrentUser();

    // Early return if no result data
    if (!result) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content" onClick={closeSidebar}>
                        <EmptyCalculator />
                    </main>
                </div>
            </div>
        );
    }

    // Extract result data
    const htsCode = result.htsno;
    const description = result.fullDescriptionChain?.length
        ? result.fullDescriptionChain.join(' → ')
        : result.description || 'No description available';

    // Get initial tariff rate from result
    const getInitialTariffRate = () => {
        if (result?.general) {
            const match = result.general.match(/([0-9.]+)/);
            return match ? parseFloat(match[1]) : 0;
        }
        return 0;
    };

    // Country selection and tariff management
    const [origin, setOrigin] = useState("Mexico");
    const {
        availableCountries,
        loading: countriesLoading,
        getNumericRate
    } = useCountryTariffs(htsCode);

    // Tariff calculation
    const {
        value,
        valueInput,
        lines,
        dutyTotal,
        dutyRatePct,
        landedCost,
        updateValue,
        updateTariffRate
    } = useTariffCalculation(10000, getInitialTariffRate());

    // Favorite management
    const {
        isFavorite,
        loading: favoriteLoading,
        toggleFavorite
    } = useFavorite(user.userId, htsCode);

    // Update tariff rate when countries load (initial setup)
    useEffect(() => {
        if (availableCountries.length > 0) {
            const numericRate = getNumericRate(origin);
            updateTariffRate(numericRate);
        }
    }, [availableCountries.length]); // Only run when countries first load

    // Handle origin change
    const handleOriginChange = (newOrigin) => {
        setOrigin(newOrigin);
        const numericRate = getNumericRate(newOrigin);
        updateTariffRate(numericRate);
    };

    // Handle favorite toggle with error handling
    const handleToggleFavorite = async () => {
        try {
            await toggleFavorite();
        } catch (error) {
            alert('Failed to update favorite. Please try again.');
        }
    };

    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />

                <main className="main-content" onClick={closeSidebar}>
                    <section className="hero-section">
                        <h1 className="hero-title">Tariff Simulator</h1>
                        <p className="hero-subtitle">
                            Estimate duties and landed cost for your shipment.
                        </p>
                    </section>

                    <div className="calc-grid">
                        <CalculatorInputs
                            keyword={keyword}
                            htsCode={htsCode}
                            description={description}
                            value={valueInput}
                            onValueChange={updateValue}
                            origin={origin}
                            onOriginChange={handleOriginChange}
                            availableCountries={availableCountries}
                            loading={countriesLoading}
                            isFavorite={isFavorite}
                            favoriteLoading={favoriteLoading}
                            onToggleFavorite={handleToggleFavorite}
                        />

                        <CalculationResults
                            htsCode={htsCode}
                            dutyRatePct={dutyRatePct}
                            baseValue={value}
                            dutyTotal={dutyTotal}
                            landedCost={landedCost}
                            lines={lines}
                        />
                    </div>

                    <DataVisualizationsSection
                        htsCode={htsCode}
                        origin={origin}
                    />
                </main>
            </div>
        </div>
    );
}
