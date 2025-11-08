import React, { useState, useEffect } from 'react';
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
import api from '../api/AxiosConfig.jsx';

function PriceHistoryChart({ hts, origin }) {
    const [chartData, setChartData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchHistoricalPrices = async () => {
            if (!hts || !origin) {
                return; // Don't fetch if parameters are missing
            }

            setLoading(true);
            setError(null);

            try {
                const countryCode = getCode(origin);

                // Call backend API
                const response = await api.get(`/product/price/${hts}/${countryCode}`);
                console.log("Line chart data:", response.data);

                // Format data: Convert Map to Array of objects
                const formattedData = Object.entries(response.data).map(([date, price]) => ({
                    date: date,
                    price: extractNumericRate(price),
                    // Format date for display
                    displayDate: new Date(date).toLocaleDateString()
                }));

                // Sort by date (oldest to newest)
                formattedData.sort((a, b) => new Date(a.date) - new Date(b.date));

                console.log("Formatted Data:", formattedData);
                setChartData(formattedData);

            } catch (error) {
                console.error("Error fetching historical prices:", error);
                setError(error.response?.data || error.message);
            } finally {
                setLoading(false);
            }
        };

        fetchHistoricalPrices();
    }, [hts, origin]);

    const extractNumericRate = (rateString) => {
        if (!rateString || rateString === 'Free' || rateString === 'N/A') return 0;
        const match = rateString.match(/([0-9.]+)/);
        return match ? parseFloat(match[1]) : 0;
    };

    if (loading) {
        return (
            <div style={{ textAlign: 'center', padding: '20px' }}>
                Loading price history...
            </div>
        );
    }

    if (error) {
        return (
            <div style={{ color: 'red', padding: '20px' }}>
                Error: {error}
            </div>
        );
    }

    if (chartData.length === 0) {
        return (
            <div style={{ padding: '20px' }}>
                No historical price data available for HTS: {hts}, Origin: {origin}
            </div>
        );
    }

    return (
        <div style={{ width: '100%', height: '500px' }}>
            <h3>Price History for {origin}</h3>
            <ResponsiveContainer width="100%" height={400}>
                <LineChart
                    data={chartData}
                    margin={{ top: 5, right: 30, left: 20, bottom: 20 }}
                >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                        dataKey="displayDate"
                        label={{ value: 'Date', position: 'insideBottom', offset: -10 }}
                        angle={-45}
                        textAnchor="end"
                        height={80}
                    />
                    <YAxis
                        label={{ value: 'Price', angle: -90, position: 'insideLeft' }}
                    />
                    <Tooltip />
                    <Legend verticalAlign="top" height={36} />
                    <Line
                        type="monotone"
                        dataKey="price"
                        stroke="#8884d8"
                        strokeWidth={2}
                        name="Price"
                        dot={{ r: 5 }}
                        activeDot={{ r: 8 }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}

export default PriceHistoryChart;
