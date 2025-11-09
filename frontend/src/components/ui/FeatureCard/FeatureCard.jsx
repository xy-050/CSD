export function FeatureCard({ icon, iconColor, title, description, buttonText, onClick }) {
    return (
        <article className="feature-card">
            <div className="feature-header">
                <div className={`feature-icon ${iconColor}`}>{icon}</div>
                <h3>{title}</h3>
            </div>
            <p>{description}</p>
            <button className="feature-btn" onClick={onClick}>
                {buttonText}
            </button>
        </article>
    );
}
