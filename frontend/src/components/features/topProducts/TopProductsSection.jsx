import { ErrorMessage } from '../../ui/ErrorMessage/ErrorMessage';

export function TopProductsSection({ products, loading, error, onProductClick }) {
    if (loading) {
        return (
            <section className="top-products">
                <h2>Top 10 Most Queried Products</h2>
                <div className="loading">Loading top products…</div>
            </section>
        );
    }

    if (error) {
        return (
            <section className="top-products">
                <h2>Top 10 Most Queried Products</h2>
                <ErrorMessage message={error} />
            </section>
        );
    }

    if (products.length === 0) {
        return (
            <section className="top-products">
                <h2>Top 10 Most Queried Products</h2>
                <p className="empty-state">
                    No queries yet. Start searching to see popular products!
                </p>
            </section>
        );
    }

    return (
        <section className="top-products">
            <h2>Top 10 Most Queried Products</h2>
            <div className="query-cards-container">
                {/* Top 3 Row - Podium Layout */}
                {products.slice(0, 3).length > 0 && (
                    <TopThreeProducts products={products.slice(0, 3)} onProductClick={onProductClick} />
                )}

                {/* Remaining 7 Row */}
                {products.slice(3, 10).length > 0 && (
                    <RemainingProducts products={products.slice(3, 10)} onProductClick={onProductClick} />
                )}
            </div>
        </section>
    );
}

function TopThreeProducts({ products, onProductClick }) {
    return (
        <div className="query-cards-row top-three">
            {/* #2 - Left */}
            {products[1] && (
                <ProductCard
                    product={products[1]}
                    rank={2}
                    className="rank-2"
                    onClick={() => onProductClick(products[1])}
                />
            )}

            {/* #1 - Center (Tallest) */}
            {products[0] && (
                <ProductCard
                    product={products[0]}
                    rank={1}
                    className="rank-1"
                    onClick={() => onProductClick(products[0])}
                />
            )}

            {/* #3 - Right */}
            {products[2] && (
                <ProductCard
                    product={products[2]}
                    rank={3}
                    className="rank-3"
                    onClick={() => onProductClick(products[2])}
                />
            )}
        </div>
    );
}

function RemainingProducts({ products, onProductClick }) {
    return (
        <div className="query-cards-row remaining">
            {products.map((product, idx) => (
                <ProductCard
                    key={product.htsCode}
                    product={product}
                    rank={idx + 4}
                    className="remaining"
                    onClick={() => onProductClick(product)}
                />
            ))}
        </div>
    );
}

function ProductCard({ product, rank, className, onClick }) {
    return (
        <div
            className={`query-card ${className}`}
            onClick={onClick}
        >
            <div className="query-rank">#{rank}</div>
            <div className="query-code">{product.htsCode}</div>
            <div className="query-category">{product.category}</div>
            <div className="query-description">{product.description}</div>
            <div className="query-count">{product.queryCount} queries</div>
        </div>
    );
}
