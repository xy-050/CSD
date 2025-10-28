import { useMemo, useState, useEffect } from "react";
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import NavBar from "./NavBar";
import api from '../api/AxiosConfig.jsx';
import { getNames as getCountryNames } from 'country-list'; // added import

export default function CalculatorPage({ }) {

    const location = useLocation();
    const { result, keyword } = location.state || {};
    const [userID, setUserID] = useState("");
    console.log(keyword);
    console.log(result);
    console.log('Full description chain:', result?.fullDescriptionChain);
    console.log('Single description:', result?.description);

    useEffect(() => {
        const getUserDetails = async () => {
            try {
                const response = await api.get("/currentUserDetails");
                console.log(response);
                setUserID(response.data.userId);
            } catch (error) {
                console.log(error);
            }
        }
        getUserDetails();
    }, []);

    // Handle case where result is missing
    if (!result) {
        return (
            <div className="homepage">
                <NavBar />
                <main className="main-content">
                    <div>No tariff data available. Please go back and select an item.</div>
                </main>
            </div>
        );
    }

    // seed
    const [hts, setHts] = useState(result.htsno);
    const [desc, setDesc] = useState(
        result.fullDescriptionChain && result.fullDescriptionChain.length > 0
            ? result.fullDescriptionChain.join(' → ') // Join the full chain with arrows
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

                setCountryTariffs(response.data);
                buildCountryList(response.data);
                updateTariffRate(origin, response.data);

            } catch (error) {
                console.error('Error fetching country tariffs:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchCountryTariffs();
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

    // Handle origin change
    const handleOriginChange = (newOrigin) => {
        setOrigin(newOrigin);
        updateTariffRate(newOrigin, countryTariffs);
    };

    // ⭐ favourites
    // const favNamespace = useMemo(() => `fav:${user?.email || "anon"}`, [user?.email]);
    // const favItemKey = useMemo(
    //     () => (calcQuery || desc || hts || "").trim().toLowerCase(),
    //     [calcQuery, desc, hts]
    // );
    // const [isFav, setIsFav] = useState(false);
    // useEffect(() => {
    //     const favs = new Set(JSON.parse(localStorage.getItem(favNamespace) || "[]"));
    //     setIsFav(favItemKey !== "" && favs.has(favItemKey));
    // }, [favNamespace, favItemKey]);
    // const toggleFav = () => {
    //     if (!favItemKey) return;
    //     const favs = new Set(JSON.parse(localStorage.getItem(favNamespace) || "[]"));
    //     favs.has(favItemKey) ? favs.delete(favItemKey) : favs.add(favItemKey);
    //     localStorage.setItem(favNamespace, JSON.stringify([...favs]));
    //     setIsFav(v => !v);
    // };

    // ⭐ favourites
    const [isFav, setIsFav] = useState(false);
    const [favLoading, setFavLoading] = useState(false);

    // Check if item is favourited on mount
    useEffect(() => {
        const checkFavouriteStatus = async () => {
            if (!hts) return;

            try {
                const response = await api.get(`/account/${userID}/favourites`);
                console.log("HELLO");
                console.log(response);
                setIsFav(response.data.isFavourite || false);
            } catch (error) {
                console.error('Error checking favourite status:', error);
            }
        };

        checkFavouriteStatus();
    }, [userID, hts]);

    // Toggle favourite status
    const toggleFav = async () => {
        if (!hts || favLoading) return;

        setFavLoading(true);
        try {
            if (isFav) {
                // Remove from favourites
                await api.delete(`/account/${userID}/favourites`, null, {
                    params: { htsCode: hts }
                });
                setIsFav(false);
            } else {
                // Add to favourites
                await api.post(`/account/${userID}/favourites`, null,{
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

    // calc - Initialize with actual tariff rate
    const getInitialTariffRate = () => {
        if (result?.general) {
            // Extract percentage from strings like "5.6%" or "Free"
            const match = result.general.match(/([0-9.]+)/);
            return match ? parseFloat(match[1]) : 0;
        }
        return 0;
    };

    const [lines, setLines] = useState([
        { label: "Tariff Duty", base: value, ratePct: getInitialTariffRate() },
    ]);
    // const hmf = useMemo(() => Math.round(value * 0.0013), [value]);
    // const mpf = 35;
    const dutyTotal = useMemo(() => lines.reduce((s, l) => s + l.base * (l.ratePct / 100), 0), [lines]);
    const dutyRatePct = useMemo(() => (value ? (dutyTotal / value) * 100 : 0), [dutyTotal, value]);
    const landedCost = value + dutyTotal; // + hmf + mpf; (commented out for demo)

    const updateLine = (i, patch) => setLines(p => p.map((l, idx) => (idx === i ? { ...l, ...patch } : l)));
    const addLine = () => setLines(p => [...p, { label: `Line ${p.length + 1}`, base: 0, ratePct: 0 }]);
    const removeLine = (i) => setLines(p => p.filter((_, idx) => idx !== i));

    return (
        <div className="homepage">
            {/* Shared navbar */}
            <NavBar />

            {/* Main */}
            <main className="main-content">
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
                                <h3 className="calc-title">{keyword}</h3>
                            </div>
                            <button
                                type="button"
                                className="star-btn"
                                aria-pressed={isFav}
                                title={isFav ? "Unfavourite" : "Favourite"}
                                onClick={toggleFav}
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

                        <div className="form-row">
                            <label>Shipment Value (USD)</label>
                            <input type="number" className="search-input" value={valueInput}
                                onChange={e => {
                                    const inputValue = e.target.value;
                                    setValueInput(inputValue);

                                    // Convert to number for calculations, default to 0 if empty or invalid
                                    const numericValue = inputValue === '' || isNaN(Number(inputValue)) ? 0 : Number(inputValue);
                                    setValue(numericValue);

                                    // Update the first duty line base to match shipment value
                                    setLines(prevLines => [
                                        { ...prevLines[0], base: numericValue },
                                        ...prevLines.slice(1)
                                    ]);
                                }} />
                        </div>

                        <div className="form-row two">
                            <div>
                                <label>Country of Origin</label>
                                {loading ? (
                                    <div className="search-input">Loading countries...</div>
                                ) : (
                                    <select className="search-input" value={origin} onChange={e => handleOriginChange(e.target.value)}>
                                        {availableCountries.map((country, index) => (
                                            <option key={index} value={country.name}>
                                                {country.name} - {country.rate}
                                            </option>
                                        ))}
                                        {/* Fallback if API fails */}
                                        {availableCountries.length === 0 && (
                                            <option value="Mexico">Mexico</option>
                                        )}
                                    </select>
                                )}
                            </div>
                            <div>
                                <label>Import Programs</label>
                                <select className="search-input" value={program} onChange={e => setProgram(e.target.value)}>
                                    <option value="none">None</option>
                                    <option value="ieepa25">IEEPA 25%</option>
                                    <option value="ieepa50">IEEPA 50%</option>
                                </select>
                            </div>
                        </div>

                        <div className="form-row two">
                            <div>
                                <label>Mode of Transport</label>
                                <select className="search-input" value={transport} onChange={e => setTransport(e.target.value)}>
                                    <option value="OCEAN">🚢 Ocean</option>
                                    <option value="AIR">✈️ Air</option>
                                    <option value="TRUCK">🚚 Truck</option>
                                </select>
                            </div>
                            <div>
                                <label>Entry Date</label>
                                <input type="date" className="search-input" value={entryDate} onChange={e => setEntryDate(e.target.value)} />
                            </div>
                        </div>

                        <div className="form-row two">
                            <div>
                                <label>Date of Loading</label>
                                <input type="date" className="search-input" value={loadDate} onChange={e => setLoadDate(e.target.value)} />
                            </div>
                            {/* <div>
                                <label>Current Tariff Rate</label>
                                <div className="search-input" style={{backgroundColor: '#f8f9fa', fontWeight: 'bold', color: currentTariffRate === 'Free' ? '#28a745' : '#007bff'}}>
                                    {currentTariffRate || 'Loading...'}
                                </div>
                            </div> */}
                        </div>

                        <div className="form-row">
                            <div className="lines-header">
                                <label>Duty Lines</label>
                                <button type="button" className="feature-btn" onClick={addLine}>Add line</button>
                            </div>
                            <div className="lines-list">
                                {lines.map((l, i) => (
                                    <div key={i} className="line-item">
                                        <input className="search-input" value={l.label}
                                            onChange={e => updateLine(i, { label: e.target.value })} />
                                        <input type="number" className="search-input" value={l.base}
                                            onChange={e => updateLine(i, { base: Number(e.target.value) || 0 })} placeholder="Value (USD)" />
                                        <div className="inline">
                                            <input type="number" className="search-input" value={l.ratePct}
                                                onChange={e => updateLine(i, { ratePct: Number(e.target.value) || 0 })} />
                                            <span className="pct">%</span>
                                        </div>
                                        <button type="button" className="chip danger" onClick={() => removeLine(i)}>✕</button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </section>

                    {/* RIGHT: Results */}
                    <aside className="calc-card">
                        <h3 className="calc-title">Calculation Results</h3>

                        <div className="results-top">
                            <div>
                                <div className="muted">DUTY RATE</div>
                                <div className="duty-rate">{dutyRatePct.toFixed(2)}%</div>
                            </div>

                            <div className="breakdown">
                                <div className="muted">COST BREAKDOWN</div>
                                <div className="row"><span>Base Cost</span><b>${value.toLocaleString()}</b></div>
                                <div className="row"><span>Total Duties</span><b>${dutyTotal.toLocaleString()}</b></div>
                                {/* <div className="row small"><span>Harbor Maintenance Fee (HMF)</span><b>${hmf.toLocaleString()}</b></div>
                                <div className="row small"><span>Merchandise Processing Fee (MPF)</span><b>${mpf.toLocaleString()}</b></div> */}
                                <div className="row total"><span>Landed Cost</span><b>${landedCost.toLocaleString()}</b></div>
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
            </main>
        </div>
    );
}