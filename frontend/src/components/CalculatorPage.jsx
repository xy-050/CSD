import { useMemo, useState, useEffect } from "react";
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import NavBar from "./NavBar";

export default function CalculatorPage({  }) {

    const location = useLocation();
    const { result, keyword } = location.state || {};
    console.log(keyword);
    console.log(result);
    console.log(result.description)

    // seed
    const [hts, setHts] = useState(result.htsno);
    const [desc, setDesc] = useState(result.description);
    const [value, setValue] = useState(10000);
    const [origin, setOrigin] = useState("MX");
    const [program, setProgram] = useState("none");
    const [transport, setTransport] = useState("OCEAN");
    const [entryDate, setEntryDate] = useState(new Date().toISOString().slice(0, 10));
    const [loadDate, setLoadDate] = useState(new Date().toISOString().slice(0, 10));

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

    // calc
    const [lines, setLines] = useState([
        { label: "Line 1", base: 7000, ratePct: 25 },
        { label: "Line 2", base: 3000, ratePct: 50 },
    ]);
    const hmf = useMemo(() => Math.round(value * 0.0013), [value]);
    const mpf = 35;
    const dutyTotal = useMemo(() => lines.reduce((s, l) => s + l.base * (l.ratePct / 100), 0), [lines]);
    const dutyRatePct = useMemo(() => (value ? (dutyTotal / value) * 100 : 0), [dutyTotal, value]);
    const landedCost = value + dutyTotal + hmf + mpf;

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
                            {/* <button
                                type="button"
                                className="star-btn"
                                aria-pressed={isFav}
                                title={isFav ? "Unfavorite" : "Favorite"}
                                onClick={toggleFav}
                            >
                                ★
                            </button> */}
                        </div>

                        <div className="form-row">
                            <label>HTS Code</label>
                            <input className="search-input" value={hts} onChange={e => setHts(e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Commodity Description</label>
                            <input className="search-input" value={desc} onChange={e => setDesc(e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Shipment Value (USD)</label>
                            <input type="number" className="search-input" value={value}
                                onChange={e => setValue(Number(e.target.value) || 0)} />
                        </div>

                        <div className="form-row two">
                            <div>
                                <label>Country of Origin</label>
                                <select className="search-input" value={origin} onChange={e => setOrigin(e.target.value)}>
                                    <option value="MX">🇲🇽 Mexico (MX)</option>
                                    <option value="US">🇺🇸 United States (US)</option>
                                    <option value="CN">🇨🇳 China (CN)</option>
                                    <option value="CA">🇨🇦 Canada (CA)</option>
                                </select>
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
                                <div className="row small"><span>Harbor Maintenance Fee (HMF)</span><b>${hmf.toLocaleString()}</b></div>
                                <div className="row small"><span>Merchandise Processing Fee (MPF)</span><b>${mpf.toLocaleString()}</b></div>
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