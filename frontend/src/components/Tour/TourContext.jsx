// ============================================
// REACT 19 COMPATIBLE TOUR 🎉
// ============================================
// Install: npm install @reactour/tour
// ============================================

import { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    TourProvider as ReactourProvider,
    useTour as useReactour,
} from '@reactour/tour';

// --------------------------------------------------
// 🧠 Step definitions for each page
// --------------------------------------------------
const allSteps = {
    home: [
        {
            selector: '[data-tour="hero-title"]',
            content: 'Welcome to your tariff dashboard! Let me show you how to calculate import duties.',
        },
        {
            selector: '[data-tour="category-buttons"]',
            content: 'Try searching for "milk". Go ahead and click the Milk button!',
            highlightedSelectors: ['[data-tour="category-milk"]'],
            mutationObservables: ['[data-tour="category-milk"]'],
            resizeObservables: ['[data-tour="category-milk"]'],
        },
    ],
    results: [
        {
            selector: '[data-tour="search-results-header"]',
            content: 'Great! Here are your search results. Each card shows an HTS code and description.',
        },
        {
            selector: '[data-tour="result-item"]',
            content: 'Click on a result to calculate tariffs. Look for items with "General Tariff" shown!',
        },
    ],
    calculator: [
        {
            selector: '[data-tour="calc-title"]',
            content: "Perfect! You've reached the Tariff Calculator. This is where the magic happens.",
        },
        {
            selector: '[data-tour="star-button"]',
            content: 'You can favorite items for quick access later by clicking this star.',
        },
        {
            selector: '[data-tour="shipment-value"]',
            content: 'Enter your shipment value in USD. This is the cost of goods.',
        },
        {
            selector: '[data-tour="country-origin"]',
            content: 'Select the country of origin. Different countries have different tariff rates!',
        },
        {
            selector: '[data-tour="transport-mode"]',
            content: 'Choose your mode of transport: Ocean, Air, or Truck.',
        },
        {
            selector: '[data-tour="calc-results"]',
            content: 'Your calculation results appear here in real-time as you adjust values.',
        },
        {
            selector: '[data-tour="duty-rate"]',
            content: 'This shows your total duty rate as a percentage of the shipment value.',
        },
        {
            selector: '[data-tour="landed-cost"]',
            content: "And here's your final landed cost — the total you'll pay including duties!",
        },
        {
            selector: '[data-tour="sidebar-favourites"]',
            content: '🎉 Great job! You can now find your favorited items here anytime. Click to explore!',
        },
    ],
    favourites: [
        {
            selector: 'main',
            content: '🎊 Congratulations! You\'ve completed the tour. Your favorited items will appear here for quick access.',
        },
    ],
};

// Calculate total steps for proper badge numbering
const getTotalSteps = () => {
    return Object.values(allSteps).reduce((total, steps) => total + steps.length, 0);
};

// Get the absolute step number across all pages
const getAbsoluteStepNumber = (pageName, currentStep) => {
    const pages = ['home', 'results', 'calculator', 'favourites'];
    let absoluteStep = 0;
    
    for (const page of pages) {
        if (page === pageName) {
            return absoluteStep + currentStep + 1;
        }
        absoluteStep += allSteps[page]?.length || 0;
    }
    return currentStep + 1;
};

// --------------------------------------------------
// 🧩 Context setup
// --------------------------------------------------
const TourContext = createContext();

// --------------------------------------------------
// 🎮 TourController – manages tour logic & persistence
// --------------------------------------------------
function TourController({ children, currentPageFromProvider }) {
    const navigate = useNavigate();
    const location = useLocation();
    const { setIsOpen, setSteps, setCurrentStep } = useReactour();

    const [tourState, setTourState] = useState(() => {
        const saved = localStorage.getItem('tariffTourState');
        if (saved) {
            try {
                return JSON.parse(saved);
            } catch {
                return { isActive: false, currentPage: 'home', completed: false };
            }
        }
        return { isActive: false, currentPage: 'home', completed: false };
    });

    // Persist state to localStorage
    useEffect(() => {
        localStorage.setItem('tariffTourState', JSON.stringify(tourState));
    }, [tourState]);

    // Update steps when route changes during an active tour
    useEffect(() => {
        if (!tourState.isActive) return;

        const path = location.pathname.replace('/', '') || 'home';
        let pageName = 'home';

        if (path === 'results') pageName = 'results';
        else if (path === 'calculator') pageName = 'calculator';
        else if (path === 'favourites') pageName = 'favourites';

        // Only update if we're actually changing pages AND we have steps for this page
        if (pageName !== tourState.currentPage && allSteps[pageName]) {
            setTimeout(() => {
                setTourState((prev) => ({ 
                    ...prev, 
                    currentPage: pageName,
                }));
                setSteps(allSteps[pageName]);
                setCurrentStep(0);
                setIsOpen(true);
            }, 500);
        }
    }, [location.pathname, tourState.isActive, tourState.currentPage, setSteps, setCurrentStep, setIsOpen]);

    // Start tour
    const startTour = () => {
        navigate('/home');
        setTimeout(() => {
            setTourState({ isActive: true, currentPage: 'home', completed: false });
            setTimeout(() => {
                setSteps(allSteps.home);
                setCurrentStep(0);
                setIsOpen(true);
            }, 500);
        }, 300);
    };

    // Complete tour
    const completeTour = () => {
        setTourState({ isActive: false, currentPage: 'home', completed: true });
        localStorage.setItem('tariffTourCompleted', 'true');
        setIsOpen(false);
    };

    // Auto-start for first-time users
    useEffect(() => {
        const hasCompletedTour = localStorage.getItem('tariffTourCompleted');
        if (!hasCompletedTour && location.pathname.includes('home') && !tourState.isActive) {
            const timer = setTimeout(startTour, 1000);
            return () => clearTimeout(timer);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <TourContext.Provider value={{ tourState, startTour, completeTour }}>
            {children}
        </TourContext.Provider>
    );
}

// --------------------------------------------------
// 🧠 TourProvider – wraps the app and sets global tour config
// --------------------------------------------------
export function TourProvider({ children }) {
    const totalSteps = getTotalSteps();
    
    // Track current page for button logic
    const [currentPage, setCurrentPage] = useState('home');
    const location = useLocation();
    
    useEffect(() => {
        const path = location.pathname.replace('/', '') || 'home';
        let pageName = 'home';
        if (path === 'results') pageName = 'results';
        else if (path === 'calculator') pageName = 'calculator';
        else if (path === 'favourites') pageName = 'favourites';
        setCurrentPage(pageName);
    }, [location.pathname]);
    
    return (
        <ReactourProvider
            steps={allSteps.home}
            styles={{
                popover: (base) => ({
                    ...base,
                    borderRadius: 12,
                    padding: 20,
                    maxWidth: 400,
                }),
                maskArea: (base) => ({ 
                    ...base, 
                    rx: 8,
                }),
                badge: (base) => ({
                    ...base,
                    backgroundColor: '#4F46E5',
                }),
                controls: (base) => ({ ...base, marginTop: 20 }),
                close: (base) => ({ ...base, color: '#6B7280' }),
            }}
            showNavigation
            showBadge
            showCloseButton
            disableDotsNavigation={false}
            padding={10}
            // Custom badge to show global step numbers
            badgeContent={({ currentStep }) => {
                const absoluteStep = getAbsoluteStepNumber(currentPage, currentStep);
                return `${absoluteStep}/${totalSteps}`;
            }}
            prevButton={({ currentStep, setCurrentStep }) => (
                <button
                    onClick={() => setCurrentStep(Math.max(0, currentStep - 1))}
                    style={{
                        padding: '10px 20px',
                        borderRadius: 8,
                        border: '1px solid #4F46E5',
                        backgroundColor: 'white',
                        color: '#4F46E5',
                        cursor: 'pointer',
                        marginRight: 10,
                    }}
                >
                    Back
                </button>
            )}
            nextButton={({ currentStep, stepsLength, setIsOpen, setCurrentStep }) => {
                // Check if this is truly the last step of the entire tour
                // Only show "Finish" if we're on the favourites page AND on the last step
                const isLastStepOfTour = (
                    currentPage === 'favourites' && 
                    currentStep === stepsLength - 1
                );
                
                return (
                    <button
                        onClick={() => {
                            if (isLastStepOfTour) {
                                setIsOpen(false);
                            } else {
                                setCurrentStep(currentStep + 1);
                            }
                        }}
                        style={{
                            padding: '10px 20px',
                            borderRadius: 8,
                            border: 'none',
                            backgroundColor: '#4F46E5',
                            color: 'white',
                            cursor: 'pointer',
                        }}
                    >
                        {isLastStepOfTour ? 'Finish' : 'Next'}
                    </button>
                );
            }}
            onClickMask={({ setIsOpen }) => setIsOpen(false)}
        >
            <TourController currentPageFromProvider={currentPage}>{children}</TourController>
        </ReactourProvider>
    );
}

// --------------------------------------------------
// 🎯 useTour hook for child components
// --------------------------------------------------
export const useTour = () => {
    const context = useContext(TourContext);
    if (!context) {
        throw new Error('useTour must be used within TourProvider');
    }
    return context;
};