export function TourButton({ onClick }) {
    return (
        <button className="tour-start-btn" onClick={onClick}>
            <span className="tour-btn-icon">🎓</span>
            Take a Quick Tour
        </button>
    );
}
