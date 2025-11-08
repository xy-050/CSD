import { memo } from 'react';
import PriceHistoryChart from "./LineChart.jsx";
import PriceWorldmap from "./WorldMap/WorldMap.jsx";

export const DataVisualizationsSection = memo(function DataVisualizationsSection({
    htsCode,
    origin
}) {
    return (
        <section className="charts">
            <h2 className="charts-title">Data Visualizations</h2>

            <div className="chart-wrapper" data-tour="price-history-chart">
                <PriceHistoryChart hts={htsCode} origin={origin} />
            </div>

            <div className="chart-wrapper" data-tour="price-distribution-map">
                <PriceWorldmap htsCode={htsCode} />
            </div>
        </section>
    );
});
