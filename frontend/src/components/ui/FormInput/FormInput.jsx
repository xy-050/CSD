export function FormInput({
    label,
    icon,
    type = "text",
    placeholder,
    value,
    onChange,
    required = false,
    minLength,
    disabled = false
}) {
    return (
        <div className="input-group">
            <label>{label}</label>
            <div className="input-wrapper">
                <span className="input-icon">{icon}</span>
                <input
                    type={type}
                    placeholder={placeholder}
                    value={value}
                    onChange={onChange}
                    required={required}
                    minLength={minLength}
                    disabled={disabled}
                />
            </div>
        </div>
    );
}
