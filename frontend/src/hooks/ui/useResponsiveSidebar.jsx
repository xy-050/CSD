import { useState, useEffect, useCallback } from 'react';

export function useResponsiveSidebar() {
    // Initialize from localStorage, default to false (closed)
    const [isOpen, setIsOpen] = useState(() => {
        const saved = localStorage.getItem('sidebarOpen');
        return saved ? JSON.parse(saved) : false;
    });

    // Save to localStorage whenever state changes
    useEffect(() => {
        localStorage.setItem('sidebarOpen', JSON.stringify(isOpen));
    }, [isOpen]);

    // Toggle sidebar
    const toggle = useCallback(() => {
        setIsOpen(prev => !prev);
    }, []);

    // Close sidebar (for mobile)
    const closeOnMobile = useCallback(() => {
        if (window.innerWidth < 1024) {
            setIsOpen(false);
        }
    }, []);

    // Open sidebar
    const open = useCallback(() => {
        setIsOpen(true);
    }, []);

    // Close sidebar
    const close = useCallback(() => {
        setIsOpen(false);
    }, []);

    return {
        isOpen,
        toggle,
        closeOnMobile,
        open,
        close
    };
}
