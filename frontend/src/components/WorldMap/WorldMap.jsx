import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import { scaleLinear } from 'd3-scale';
import 'leaflet/dist/leaflet.css';
import '../../utils/leafletIconFix.jsx';
import { useCountryGeoJSON } from '../../hooks/useCountryGeoJSON.jsx';
import api from '../../api/AxiosConfig.jsx';

function PriceWorldMap({ htsCode }) {
    const [priceData, setPriceData] = useState({});
    const [rawPriceData, setRawPriceData] = useState({});
    const [loading, setLoading] = useState(true);
    const { geoData, loading: geoLoading, error } = useCountryGeoJSON();

    // fetch price data from backend
    useEffect(() => {
        const fetchPriceData = async () => {
            if (!htsCode) return;

            setLoading(true);
            try {
                const response = await api.get(`/product/price/map/${htsCode}`);
                console.log("Price data:", response.data);

                setRawPriceData(response.data);
                const numericPrices = convertPricesToNumeric(response.data);
                setPriceData(numericPrices);
            } catch (error) {
                console.error("Error fetching price data:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchPriceData();
    }, [htsCode]);

    // convert price strings to numbers for color scaling
    const convertPricesToNumeric = (data) => {
        const numeric = {};

        Object.entries(data).forEach(([country, price]) => {
            const match = price.match(/[\d.]+/);
            if (match) {
                numeric[country] = parseFloat(match[0]);
            } else if (price.toLowerCase().includes('free')) {
                numeric[country] = 0;
            } else {
                numeric[country] = null;
            }
        });

        return numeric;
    };

    // get color based on price
    const getColor = (countryCode) => {
        const price = priceData[countryCode];

        if (price === undefined || price === null) {
            return '#E0E0E0'; // Gray for no data
        }

        const prices = Object.values(priceData).filter(p => p !== null && p !== undefined);

        if (prices.length === 0) {
            return '#E0E0E0';
        }

        const minPrice = Math.min(...prices);
        const maxPrice = Math.max(...prices);

        // avoid division by zero if all prices are the same
        if (minPrice === maxPrice) {
            return '#6baed6';
        }

        // color scale from light blue to dark blue
        const colorScale = scaleLinear()
            .domain([minPrice, maxPrice])
            .range(['#c6dbef', '#26619cff']);

        const color = colorScale(price);
        // console.log({ countryCode, price, minPrice, maxPrice, color });
        return color;
    };

    // style for each country
    const style = (feature) => {
        const countryCode = getCountryCode(feature, feature.properties.name);

        return {
            fillColor: getColor(countryCode),
            weight: 1,
            opacity: 1,
            color: 'white',
            fillOpacity: 0.7
        };
    };

    const getCountryCode = (country, countryName) => {

        if (countryName == "Taiwan") return "TW";
        else if (countryName == "France") return "FR";
        else if (countryName == "Norway") return "NO";
        else if (countryName == "United States of America") return -99;
        else return country.properties['ISO3166-1-Alpha-2'];
    };

    // add interactivity to each country
    const onEachCountry = (country, layer) => {

        const countryName = country.properties.name;
        const countryCode = getCountryCode(country, countryName);
        const price = rawPriceData[countryCode];

        // Popup content
        const popupContent = `
            <div style="padding: 5px;">
                <strong>${countryName}</strong><br/>
                Price: ${price || 'No data'}
            </div>
        `;

        layer.bindPopup(popupContent);

        // Hover effects
        layer.on({
            mouseover: (e) => {
                const layer = e.target;
                layer.setStyle({
                    weight: 3,
                    color: '#666',
                    fillOpacity: 0.9
                });
                layer.bringToFront();
            },
            mouseout: (e) => {
                const layer = e.target;
                layer.setStyle(
                    style(country));
            }
        });
    };

    if (loading || geoLoading) {
        return (
            <div style={{ textAlign: 'center', padding: '40px' }}>
                <p>Loading map...</p>
            </div>
        );
    }

    if (error || !geoData) {
        return (
            <div style={{ textAlign: 'center', padding: '40px', color: 'red' }}>
                <p>Error loading map data</p>
            </div>
        );
    }

    if (Object.keys(priceData).length === 0) {
        return (
            <div style={{ textAlign: 'center', padding: '40px' }}>
                <p>No price data available for HTS code: {htsCode}</p>
            </div>
        );
    }

    return (
        <div style={{ width: '100%', padding: '20px' }}>
            <h3 style={{ marginBottom: '20px' }}>
                Price Distribution by Country:
            </h3>

            {/* Map Container */}
            <div style={{
                height: '600px',
                width: '100%',
                border: '1px solid #ddd',
                borderRadius: '8px',
                overflow: 'hidden'
            }}>
                <MapContainer
                    center={[20, 0]}
                    zoom={2}
                    minZoom={2}
                    maxZoom={6}
                    style={{ height: '100%', width: '100%' }}
                    scrollWheelZoom={true}
                >
                    {/* Base map layer */}
                    <TileLayer
                        attribution='&copy; <a href="<https://www.openstreetmap.org/copyright>">OpenStreetMap</a> contributors'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    />

                    {/* Country boundaries with colors */}
                    <GeoJSON
                        data={geoData}
                        style={style}
                        onEachFeature={onEachCountry}
                    />
                </MapContainer>
            </div>

            {/* Legend */}
            <div style={{
                marginTop: '20px',
                padding: '15px',
                backgroundColor: '#f5f5f5',
                borderRadius: '8px'
            }}>
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '15px',
                    flexWrap: 'wrap'
                }}>
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>
                        Lower Price
                    </span>
                    <div style={{
                        width: '250px',
                        height: '25px',
                        background: 'linear-gradient(to right, #c6dbef, #08519c)',
                        borderRadius: '4px',
                        border: '1px solid #ddd'
                    }} />
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>
                        Higher Price
                    </span>
                </div>
                <div style={{
                    marginTop: '10px',
                    textAlign: 'center',
                    fontSize: '12px',
                    color: '#666'
                }}>
                    Gray = No data available | Click countries for details
                </div>
            </div>
        </div>
    );
}

export default PriceWorldMap;
