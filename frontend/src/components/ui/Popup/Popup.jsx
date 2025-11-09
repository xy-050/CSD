import React from 'react';
import './Popup.css';

const Popup = ({ children, onClose }) => {
  return (
    <div className="popup-overlay" onClick={onClose}>
      <div className="popup-content" onClick={e => e.stopPropagation()}>
        <button className="popup-close-btn" onClick={onClose}>
          &times;
        </button>
        {children}
      </div>
    </div>
  );
};

export default Popup;