import { useState, useEffect } from 'react';

export function useTypingEffect(text, speed = 60) {
    const [typedText, setTypedText] = useState("");
    const [isDone, setIsDone] = useState(false);

    useEffect(() => {
        let i = 0;
        const interval = setInterval(() => {
            i += 1;
            setTypedText(text.slice(0, i));
            if (i >= text.length) {
                clearInterval(interval);
                setIsDone(true);
            }
        }, speed);

        return () => clearInterval(interval);
    }, [text, speed]);

    return { typedText, isDone };
}
