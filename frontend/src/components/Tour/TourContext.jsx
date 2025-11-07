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
            content: 'Try searching for "bread". Go ahead and click the Bread button!',
            highlightedSelectors: ['[data-tour="category-bread"]'],
            mutationObservables: ['[data-tour="category-bread"]'],
            resizeObservables: ['[data-tour="category-bread"]'],
        },
    ],

    results: [
        { selector: '[data-tour="search-results-header"]', content: 'Great! Here are your search results.' },
        { selector: '[data-tour="result-parent"]', content: 'Click "See More" to expand this category.' },
        { selector: '[data-tour="result-item"]', content: 'Click on a result with "General Tariff" to calculate duties.' },
    ],

    calculator: [
        { selector: '[data-tour="calc-title"]', content: "Perfect! You've reached the Tariff Calculator." },
        { selector: '[data-tour="star-button"]', content: 'You can favorite items for quick access later.' },
        { selector: '[data-tour="shipment-value"]', content: 'Enter your shipment value in USD.' },
        { selector: '[data-tour="country-origin"]', content: 'Select the country of origin.' },
        { selector: '[data-tour="calc-results"]', content: 'Your calculation results appear here in real-time.' },
        { selector: '[data-tour="duty-rate"]', content: 'This shows your total duty rate.' },
        { selector: '[data-tour="landed-cost"]', content: "And here's your final landed cost." },
        {
            selector: '[data-tour="nav-brand"]',
            content: "That's it! Click here to return home whenever you're ready. 🎉",
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
function TourController({ children }) {
    const navigate = useNavigate();
    const location = useLocation();
    const { setIsOpen, setSteps, setCurrentStep } = useReactour();

    const [tourState, setTourState] = useState({
        isActive: false,
        currentPage: 'home',
        completed: false,
    });

    useEffect(() => {
        if (!tourState.isActive || tourState.completed) return;

        const path = location.pathname.replace('/', '') || 'home';
        let pageName = 'home';
        if (path === 'results') pageName = 'results';
        else if (path === 'calculator') pageName = 'calculator';

        if (pageName !== tourState.currentPage && allSteps[pageName]) {
            // 👇 NEW: if user goes from calculator → home while tour is active,
            // treat it as completing the tour (do NOT restart on home)
            if (tourState.currentPage === 'calculator' && pageName === 'home') {
                setTourState(prev => ({
                    ...prev,
                    isActive: false,
                    completed: true,
                    currentPage: 'home',
                }));
                setIsOpen(false);
                return; // 🔚 stop, don't reset steps for 'home'
            }

            // Normal behavior: moving forward between pages during the tour
            setTimeout(() => {
                setTourState(prev => ({ ...prev, currentPage: pageName }));
                setSteps(allSteps[pageName]);
                setCurrentStep(0);
                setIsOpen(true);
            }, 100);
        }
    }, [location.pathname, tourState.isActive, tourState.completed, tourState.currentPage, setSteps, setCurrentStep, setIsOpen]);


    const startTour = () => {
        navigate('/home');
        setTimeout(() => {
            setTourState({ isActive: true, currentPage: 'home', completed: false });
            setTimeout(() => {
                setSteps(allSteps.home);
                setCurrentStep(0);
                setIsOpen(true);
            }, 100);
        }, 300);
    };

    const completeTour = () => {
        setTourState({ isActive: false, currentPage: 'home', completed: true });
        setIsOpen(false);
    };

    return (
        <TourContext.Provider value={{ tourState, startTour, completeTour, setCurrentStep }}>
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
                const isLastStepOfTour =
                    currentPage === 'calculator' && currentStep === stepsLength - 1;

                return (
                    <button
                        onClick={() => {
                            if (isLastStepOfTour) {
                                // Finish tour at last calculator step
                                setIsOpen(false);
                                completeTour(); // mark as completed
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