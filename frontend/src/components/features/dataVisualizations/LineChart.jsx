import { useState, useEffect, useCallback } from 'react';
import { getCode } from 'country-list';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer
} from 'recharts';

import api from '../../../api/AxiosConfig';

// Move helper function outside component
const extractNumericRate = (rateString) => {
    if (!rateString || rateString === 'Free' || rateString === 'N/A') return 0;
    const match = rateString.match(/([0-9.]+)/);
    return match ? parseFloat(match[1]) : 0;
};

function PriceHistoryChart({ hts, origin }) {
    const [chartData, setChartData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Memoize fetch function
    const fetchHistoricalPrices = useCallback(async () => {
        if (!hts || !origin) {
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const countryCode = getCode(origin);

            if (!countryCode) {
                throw new Error(`Invalid country: ${origin}`);
            }

            const response = await api.get(`/product/price/${hts}/${countryCode}`);
            console.log("Line chart data:", response.data);

            const formattedData = Object.entries(response.data).map(([date, price]) => ({
                date: date,
                price: extractNumericRate(price),
                displayDate: new Date(date).toLocaleDateString()
            }));

            formattedData.sort((a, b) => new Date(a.date) - new Date(b.date));
            console.log("Formatted Data:", formattedData);

            setChartData(formattedData);
        } catch (error) {
            console.error("Error fetching historical prices:", error);
            setError(error.response?.data?.message || error.message || 'Failed to load data');
        } finally {
            setLoading(false);
        }
    }, [hts, origin]);

    useEffect(() => {
        fetchHistoricalPrices();
    }, [fetchHistoricalPrices]);

    if (loading) {
        return (
            <div className="chart-loading">
                <div className="loading-spinner">⏳</div>
                <p>Loading price history...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="chart-error">
                <div className="error-icon">⚠️</div>
                <p>Error: {error}</p>
            </div>
        );
    }

    if (chartData.length === 0) {
        return (
            <div className="chart-empty">
                <div className="empty-icon">📊</div>
                <p>No historical price data available</p>
                <small>HTS: {hts} | Origin: {origin}</small>
            </div>
        );
    }

    return (
        <div className="price-chart-container">
            <h3 className="chart-title">Price History for {origin}</h3>
            <ResponsiveContainer width="100%" height={400}>
                <LineChart
                    data={chartData}
                    margin={{ top: 5, right: 30, left: 20, bottom: 20 }}
                >
                    <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
                    <XAxis
                        dataKey="displayDate"
                        label={{ value: 'Date', position: 'insideBottom', offset: -10 }}
                        angle={-45}
                        textAnchor="end"
                        height={80}
                        tick={{ fontSize: 12 }}
                    />
                    <YAxis
                        label={{ value: 'Price (%)', angle: -90, position: 'insideLeft' }}
                        tick={{ fontSize: 12 }}
                    />
                    <Tooltip
                        contentStyle={{
                            backgroundColor: '#fff',
                            border: '2px solid #57B9FF',
                            borderRadius: '8px'
                        }}
                    />
                    <Legend
                        verticalAlign="top"
                        height={36}
                        wrapperStyle={{ paddingBottom: '10px' }}
                    />
                    <Line
                        type="monotone"
                        dataKey="price"
                        stroke="#57B9FF"
                        strokeWidth={3}
                        name="Tariff Rate (%)"
                        dot={{ r: 4, fill: '#57B9FF' }}
                        activeDot={{ r: 7, fill: '#3A7EC0' }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}

export default PriceHistoryChart;
