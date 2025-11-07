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
      content:
        'Welcome to your tariff dashboard! Let me show you how to calculate import duties.',
    },
    {
      selector: '[data-tour="category-buttons"]',
      content: 'Try searching for "egg". Go ahead and click the Egg button!',
      highlightedSelectors: ['[data-tour="category-egg"]'],
      mutationObservables: ['[data-tour="category-egg"]'],
      resizeObservables: ['[data-tour="category-egg"]'],
    },
  ],

  results: [
    {
      selector: '[data-tour="search-results-header"]',
      content: 'Great! Here are your search results.',
    },
    {
      selector: '[data-tour="result-parent"]',
      content: 'Click "See More" to expand this category.',
    },
    {
      selector: '[data-tour="result-item"]',
      content:
        'Click on a result with "General Tariff" to calculate duties.',
    },
  ],

  calculator: [
    {
      selector: '[data-tour="calc-title"]',
      content: "Perfect! You've reached the Tariff Calculator.",
    },
    {
      selector: '[data-tour="star-button"]',
      content: 'You can favorite items for quick access later.',
    },
    {
      selector: '[data-tour="shipment-value"]',
      content: 'Enter your shipment value in USD.',
    },
    {
      selector: '[data-tour="country-origin"]',
      content: 'Select the country of origin.',
    },
    {
      selector: '[data-tour="calc-results"]',
      content: 'Your calculation results appear here in real-time.',
    },
    {
      selector: '[data-tour="duty-rate"]',
      content: 'This shows your total duty rate.',
    },
    {
      selector: '[data-tour="landed-cost"]',
      content: "And here's your final landed cost.",
    },
    {
      selector: '[data-tour="nav-brand"]',
      content:
        "That's it! Click here to return home whenever you're ready. 🎉",
    },
  ],
};

// --------------------------------------------------
// 🔢 Step numbering helpers
// --------------------------------------------------
const getTotalSteps = () => {
  return Object.values(allSteps).reduce(
    (total, steps) => total + steps.length,
    0
  );
};

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
      // If user goes from calculator → home while tour is active,
      // treat it as completing the tour (do NOT restart on home)
      if (tourState.currentPage === 'calculator' && pageName === 'home') {
        setTourState((prev) => ({
          ...prev,
          isActive: false,
          completed: true,
          currentPage: 'home',
        }));
        setIsOpen(false);
        return;
      }

      // Normal behavior: move between pages during the tour
      setTimeout(() => {
        setTourState((prev) => ({ ...prev, currentPage: pageName }));
        setSteps(allSteps[pageName]);
        setCurrentStep(0);
        setIsOpen(true);
      }, 100);
    }
  }, [
    location.pathname,
    tourState.isActive,
    tourState.completed,
    tourState.currentPage,
    setSteps,
    setCurrentStep,
    setIsOpen,
  ]);

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
// 🎯 TourStyles 
// --------------------------------------------------
const TourContext = createContext(null);

export const tourStyles = {
  // main white box
  popover: (base) => ({
    ...base,
    borderRadius: 16,
    padding: '1.25rem 1.75rem 1.25rem 1.25rem',
    maxWidth: 360,            // keeps things compact & away from the edges
  }),

  // container that holds the dots
  navigation: (base) => ({
    ...base,
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 6,                   // even spacing between dots
    flex: 1,
  }),

  // individual dots
  dot: (base, { current }) => ({
    ...base,
    margin: 0,
    width: 8,
    height: 8,
    transform: 'none',
    boxShadow: 'none',
    opacity: current ? 1 : 0.3,
  }),
};


// --------------------------------------------------
// 🧠 TourProvider – wraps the app and sets global tour config
// --------------------------------------------------
export function TourProvider({ children }) {
  const totalSteps = getTotalSteps();

  // Track current page for badge numbering
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
      styles={tourStyles}                   // ✅ use the object directly
      showNavigation
      showBadge
      showCloseButton={false}
      disableDotsNavigation={false}
      padding={10}
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
                // Just close the tour; TourController will mark it as completed
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
      <TourController>{children}</TourController>
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
