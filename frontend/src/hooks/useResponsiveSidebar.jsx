import { useState, useEffect } from 'react';

export function useResponsiveSidebar() {
    const [isOpen, setIsOpen] = useState(window.innerWidth > 768);

    const toggle = () => setIsOpen(v => !v);

    const close = () => setIsOpen(false);

    const closeOnMobile = () => {
        if (window.innerWidth <= 768) {
            setIsOpen(false);
        }
    };

    // Open by default on desktop, closed on mobile
    useEffect(() => {
        const isMobile = window.innerWidth <= 768;
        setIsOpen(!isMobile);
    }, []);

    // ESC closes sidebar on mobile
    useEffect(() => {
        const onKey = (e) => {
            if (e.key === "Escape" && window.innerWidth <= 768) {
                setIsOpen(false);
            }
        };
        document.addEventListener("keydown", onKey);
        return () => document.removeEventListener("keydown", onKey);
    }, []);

    return { isOpen, toggle, close, closeOnMobile };
}
