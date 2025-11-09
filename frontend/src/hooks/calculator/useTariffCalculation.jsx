import { useState, useMemo, useCallback } from 'react';

export function useTariffCalculation(initialValue = 10000, initialRate = 0) {
    const [value, setValue] = useState(initialValue);
    const [valueInput, setValueInput] = useState(String(initialValue));
    const [lines, setLines] = useState([
        { label: "Tariff Duty", base: initialValue, ratePct: initialRate }
    ]);

    // Calculate total duty
    const dutyTotal = useMemo(
        () => lines.reduce((sum, line) => sum + line.base * (line.ratePct / 100), 0),
        [lines]
    );

    // Calculate duty rate percentage
    const dutyRatePct = useMemo(
        () => (value ? (dutyTotal / value) * 100 : 0),
        [dutyTotal, value]
    );

    // Calculate landed cost
    const landedCost = value + dutyTotal;

    // Update shipment value
    const updateValue = useCallback((newValue) => {
        const inputValue = String(newValue);
        setValueInput(inputValue);

        const numericValue = inputValue === '' || isNaN(Number(inputValue))
            ? 0
            : Number(inputValue);

        setValue(numericValue);
        setLines(prev => [
            { ...prev[0], base: numericValue },
            ...prev.slice(1)
        ]);
    }, []);

    // Update tariff rate (accepts numeric rate)
    const updateTariffRate = useCallback((numericRate) => {
        setLines(prev => [
            { ...prev[0], ratePct: numericRate },
            ...prev.slice(1)
        ]);
    }, []);

    // Update specific line
    const updateLine = useCallback((index, patch) => {
        setLines(prev => prev.map((line, idx) =>
            idx === index ? { ...line, ...patch } : line
        ));
    }, []);

    // Add new line
    const addLine = useCallback(() => {
        setLines(prev => [
            ...prev,
            { label: `Line ${prev.length + 1}`, base: 0, ratePct: 0 }
        ]);
    }, []);

    // Remove line
    const removeLine = useCallback((index) => {
        setLines(prev => prev.filter((_, idx) => idx !== index));
    }, []);

    return {
        value,
        valueInput,
        lines,
        dutyTotal,
        dutyRatePct,
        landedCost,
        updateValue,
        updateTariffRate,
        updateLine,
        addLine,
        removeLine
    };
}
