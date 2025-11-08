export function SuccessMessage({ message }) {
    if (!message) return null;

    const displayMessage = typeof message === 'string'
        ? message
        : message.message || 'Success!';

    return (
        <div className="success-message">
            {displayMessage}
        </div>
    );
}
