export function CalculationResults({
    htsCode,
    dutyRatePct,
    baseValue,
    dutyTotal,
    landedCost,
    lines
}) {
    return (
        <aside className="calc-card">
            <h3 className="calc-title" data-tour="calc-results-title">
                Calculation Results
            </h3>

            <div className="results-top" data-tour="calc-results">
                <div className="duty-section">
                    <div className="muted">DUTY RATE</div>
                    <div className="duty-rate" data-tour="duty-rate">
                        {dutyRatePct.toFixed(2)}%
                    </div>
                </div>

                <div className="breakdown">
                    <div className="muted">COST BREAKDOWN</div>
                    <div className="row">
                        <span>Base Cost</span>
                        <b>${baseValue.toLocaleString()}</b>
                    </div>
                    <div className="row">
                        <span>Total Duties</span>
                        <b>${dutyTotal.toLocaleString()}</b>
                    </div>
                    <div className="row total" data-tour="landed-cost">
                        <span>Landed Cost</span>
                        <b>${landedCost.toLocaleString()}</b>
                    </div>
                </div>
            </div>

            <div className="lines-table">
                {lines.map((line, index) => (
                    <div key={index} className="line-row">
                        <div className="code">{htsCode}</div>
                        <div className="pill">{line.label}</div>
                        <div className="muted">{line.ratePct}%</div>
                        <div className="amt">
                            ${(line.base * (line.ratePct / 100)).toLocaleString()}
                        </div>
                    </div>
                ))}
            </div>
        </aside>
    );
}
