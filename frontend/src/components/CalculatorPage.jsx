import { useMemo, useState, useEffect } from "react";
import { useLocation } from 'react-router-dom';
import NavBar from "./NavBar";
import Sidebar from "./Sidebar";
import api from '../api/AxiosConfig.jsx';
import { getNames as getCountryNames } from 'country-list'; // added import
import PriceHistoryChart from "./LineChart/LineChart.jsx";
import PriceWorldmap from "./WorldMap/WorldMap.jsx";

export default function CalculatorPage() {
    const location = useLocation();
    const { result, keyword } = location.state || {};
    const [userID, setUserID] = useState("");

    // sidebar
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const toggleSidebar = () => setSidebarOpen(v => !v);
    const closeSidebar = () => setSidebarOpen(false);

    useEffect(() => {
        (async () => {
            try {
                const response = await api.get("/currentUserDetails");
                setUserID(response.data.userId);
            } catch (error) {
                console.log(error);
            }
        })();
    }, []);

    if (!result) {
        return (
            <div className="homepage">
                <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />
                <div className="homepage-container">
                    <Sidebar isOpen={sidebarOpen} />
                    <main className="main-content" onClick={closeSidebar}>
                        <div>No tariff data available. Please go back and select an item.</div>
                    </main>
                </div>
            </div>
        );
    }

    // seed
    const [hts] = useState(result.htsno);
    const [desc] = useState(
        result.fullDescriptionChain?.length
            ? result.fullDescriptionChain.join(' → ')
            : result.description || 'No description available'
    );
    const [value, setValue] = useState(10000);
    const [valueInput, setValueInput] = useState('10000');
    const [origin, setOrigin] = useState("Mexico");
    const [program, setProgram] = useState("none");
    const [countryTariffs, setCountryTariffs] = useState(null);
    const [currentTariffRate, setCurrentTariffRate] = useState(null);
    const [availableCountries, setAvailableCountries] = useState([]);
    const [loading, setLoading] = useState(false);
    const [transport, setTransport] = useState("OCEAN");
    const [entryDate, setEntryDate] = useState(new Date().toISOString().slice(0, 10));
    const [loadDate, setLoadDate] = useState(new Date().toISOString().slice(0, 10));

    // Fetch country-specific tariff data
    useEffect(() => {
        const fetchCountryTariffs = async () => {
            if (!result?.htsno) return;
            setLoading(true);
            try {
                const response = await api.get(`/api/tariffs/compare-countries`, {
                    params: { htsno: result.htsno }
                });
                const data = response.data;
                setCountryTariffs(data);
                // build list then set initial rate from that same data
                const countries = [];
                if (data['Special countries']) {
                    data['Special countries'].forEach(name => {
                        countries.push({ name, rate: data['Special rate'] || 'N/A' });
                    });
                }

                const allCountries = getCountryNames();
                allCountries.forEach(name => {
                    if (!countries.find(c => c.name === name)) {
                        countries.push({ name, rate: data['General rate'] || 'N/A' });
                    }
                })
                setAvailableCountries(countries);
                // set rate for default origin
                const found = countries.find(c => c.name === origin);
                const initialRate = found ? found.rate : (data['General rate'] || 'N/A');
                setCurrentTariffRate(initialRate);
                setLines(prev => [{ ...prev[0], ratePct: extractNumericRate(initialRate) }, ...prev.slice(1)]);
            } catch (error) {
                console.error('Error fetching country tariffs:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchCountryTariffs();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [result?.htsno]);

    // Build country list from API response
    const buildCountryList = (tariffData) => {
        const countries = [];

        // Add special countries with special rates
        if (tariffData['Special countries']) {
            tariffData['Special countries'].forEach(countryName => {
                countries.push({
                    name: countryName,
                    rate: tariffData['Special rate'] || 'N/A'
                });
            });
        }

        // Use country-list package for common countries instead of hardcoded list
        const commonCountries = getCountryNames(); // all standard country names
        commonCountries.forEach(countryName => {
            if (!countries.find(c => c.name === countryName)) {
                countries.push({
                    name: countryName,
                    rate: tariffData['General rate'] || 'N/A'
                });
            }
        });

        setAvailableCountries(countries);
    };

    // Update tariff rate when origin changes
    const updateTariffRate = (selectedCountry, tariffData) => {
        if (!tariffData) return;

        const country = availableCountries.find(c => c.name === selectedCountry);
        const newRate = country ? country.rate : tariffData['General rate'] || 'N/A';
        setCurrentTariffRate(newRate);

        // Update the duty line percentage
        const numericRate = extractNumericRate(newRate);
        setLines(prevLines => [
            { ...prevLines[0], ratePct: numericRate },
            ...prevLines.slice(1)
        ]);
    };

    // Helper to extract numeric rate from strings like "5.6%" or "Free"
    const extractNumericRate = (rateString) => {
        if (!rateString || rateString === 'Free' || rateString === 'N/A') return 0;
        const match = rateString.match(/([0-9.]+)/);
        return match ? parseFloat(match[1]) : 0;
    };

    // Update tariff rate when origin changes
    const handleOriginChange = (newOrigin) => {
        setOrigin(newOrigin);
        if (!countryTariffs || !availableCountries.length) return;
        const found = availableCountries.find(c => c.name === newOrigin);
        const newRate = found ? found.rate : (countryTariffs['General rate'] || 'N/A');
        setCurrentTariffRate(newRate);
        setLines(prev => [{ ...prev[0], ratePct: extractNumericRate(newRate) }, ...prev.slice(1)]);
    };

    // ⭐ favourites
    const [isFav, setIsFav] = useState(false);
    const [favLoading, setFavLoading] = useState(false);

    // Check if favourited (your GET returns a list; not {isFavourite})
    useEffect(() => {
        (async () => {
            if (!userID || !hts) return;
            try {
                const res = await api.get(`/account/${userID}/favourites`);
                const list = Array.isArray(res.data) ? res.data : (res.data?.items || []);
                setIsFav(list.some(item => item.htsno === hts));
            } catch (e) {
                console.error('Error checking favourite status:', e);
            }
        })();
    }, [userID, hts]);

    // Toggle favourite status
    const toggleFav = async () => {
        if (!hts || favLoading) return;

        setFavLoading(true);
        try {
            if (isFav) {
                // Remove from favourites
                await api.delete(`/account/${userID}/favourites`, {
                    params: {
                        htsCode: hts
                    }
                });
                setIsFav(false);
            } else {
                // Add to favourites
                await api.post(`/account/${userID}/favourites`, null, {
                    params: {
                        htsCode: hts
                    }
                });
                setIsFav(true);
            }
        } catch (error) {
            console.error('Error toggling favourite:', error);
            // Optionally show error message to user
            alert('Failed to update favourite. Please try again.');
        } finally {
            setFavLoading(false);
        }
    };

    // calc
    const getInitialTariffRate = () => {
        if (result?.general) {
            const match = result.general.match(/([0-9.]+)/);
            return match ? parseFloat(match[1]) : 0;
        }
        return 0;
    };

    const [lines, setLines] = useState([
        { label: "Tariff Duty", base: value, ratePct: getInitialTariffRate() },
    ]);

    const dutyTotal = useMemo(
        () => lines.reduce((s, l) => s + l.base * (l.ratePct / 100), 0),
        [lines]
    );
    const dutyRatePct = useMemo(
        () => (value ? (dutyTotal / value) * 100 : 0),
        [dutyTotal, value]
    );
    const landedCost = value + dutyTotal;

    const updateLine = (i, patch) => setLines(p => p.map((l, idx) => (idx === i ? { ...l, ...patch } : l)));
    const addLine = () => setLines(p => [...p, { label: `Line ${p.length + 1}`, base: 0, ratePct: 0 }]);
    const removeLine = (i) => setLines(p => p.filter((_, idx) => idx !== i));

    return (
        <div className="homepage">
            <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />

            <div className="homepage-container">
                <Sidebar isOpen={sidebarOpen} />
                <main className="main-content" onClick={closeSidebar}>
                    <section className="hero-section">
                        <h1 className="hero-title">Tariff Simulator</h1>
                        <p className="hero-subtitle">Estimate duties and landed cost for your shipment.</p>
                    </section>

                    <div className="calc-grid">
                        {/* LEFT: Inputs */}
                        <section className="calc-card">
                            <div className="calc-card-head">
                                <div className="calc-title-wrap">
                                    <div className="mini-label">Inputs</div>
                                    <h3 className="calc-title" data-tour="calc-title">{keyword}</h3>
                                </div>
                                <button
                                    type="button"
                                    className="star-btn"
                                    data-tour="star-button"
                                    aria-pressed={isFav}
                                    title={isFav ? "Unfavourite" : "Favourite"}
                                    onClick={toggleFav}
                                    disabled={favLoading}
                                >
                                    ★
                                </button>
                            </div>

                            <div className="form-row">
                                <label>HTS Code</label>
                                <input className="search-input" value={hts} readOnly style={{ backgroundColor: '#f8f9fa', cursor: 'default' }} />
                            </div>

                            <div className="form-row">
                                <label>Commodity Description</label>
                                <input className="search-input" value={desc} readOnly style={{ backgroundColor: '#f8f9fa', cursor: 'default' }} />
                            </div>

                            <div className="form-row" data-tour="shipment-value">
                                <label>Shipment Value (USD)</label>
                                <input
                                    type="number"
                                    className="search-input"
                                    value={valueInput}
                                    onChange={e => {
                                        const inputValue = e.target.value;
                                        setValueInput(inputValue);
                                        const numericValue = inputValue === '' || isNaN(Number(inputValue)) ? 0 : Number(inputValue);
                                        setValue(numericValue);
                                        setLines(prev => [{ ...prev[0], base: numericValue }, ...prev.slice(1)]);
                                    }}
                                />
                            </div>

                            <div className="form-row two" data-tour="country-origin">
                                <div>
                                    <label>Country of Origin</label>
                                    {loading ? (
                                        <div className="search-input">Loading countries...</div>
                                    ) : (
                                        <select
                                            className="search-input"
                                            value={origin}
                                            onChange={e => handleOriginChange(e.target.value)}>
                                            {availableCountries.map((c, i) => (
                                                <option key={i} value={c.name}>
                                                    {c.name} - {c.rate}
                                                </option>
                                            ))}
                                            {availableCountries.length === 0 && <option value="Mexico">Mexico</option>}
                                        </select>
                                    )}
                                </div>
                            </div>
                        </section>

                        {/* RIGHT: Results */}
                        <aside className="calc-card">
                            <h3 className="calc-title" data-tour="calc-results-title">Calculation Results</h3>
                            <div className="results-top" data-tour="calc-results">
                                <div>
                                    <div className="muted">DUTY RATE</div>
                                    <div className="duty-rate" data-tour="duty-rate">{dutyRatePct.toFixed(2)}%</div>
                                </div>
                                <div className="breakdown">
                                    <div className="muted">COST BREAKDOWN</div>
                                    <div className="row"><span>Base Cost</span><b>${value.toLocaleString()}</b></div>
                                    <div className="row"><span>Total Duties</span><b>${dutyTotal.toLocaleString()}</b></div>
                                    <div className="row total" data-tour="landed-cost"><span>Landed Cost</span><b>${landedCost.toLocaleString()}</b></div>
                                </div>
                            </div>

                            <div className="lines-table">
                                {lines.map((l, i) => (
                                    <div key={i} className="line-row">
                                        <div className="code">{hts}</div>
                                        <div className="pill">{l.label}</div>
                                        <div className="muted">{l.ratePct}%</div>
                                        <div className="amt">${(l.base * (l.ratePct / 100)).toLocaleString()}</div>
                                    </div>
                                ))}
                            </div>
                        </aside>
                    </div>
                    <section className="charts">
                        <h2 className="charts-title"> Data Visualisations</h2>
                        {/* Line chart step */}
                        <div
                            className="chart-wrapper"
                            data-tour="price-history-chart"
                        >
                            <PriceHistoryChart hts={hts} origin={origin} />
                        </div>

                        {/* World map step */}
                        <div
                            className="chart-wrapper"
                            data-tour="price-distribution-map"
                        >
                            <h3 className="chart-subtitle">
                                Price Distribution by Country
                            </h3>
                            <PriceWorldmap htsCode={hts} />
                        </div>
                    </section>
                </main>
            </div>
        </div>
    );
}
