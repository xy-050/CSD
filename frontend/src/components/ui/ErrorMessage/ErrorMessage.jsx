export function ErrorMessage({ message }) {
    if (!message) return null;

    const displayMessage = typeof message === 'string'
        ? message
        : message.message || 'An error occurred';

    return (
        <div className="error-message">
            {displayMessage}
        </div>
    );
}
