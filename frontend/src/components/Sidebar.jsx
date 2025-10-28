import { useState } from 'react';

export default function Sidebar({ isOpen, onToggle }) {
  const menuItems = [
    {
      icon: '📈',
      label: 'Insights'
    },
    {
      icon: '⭐️',
      label: 'Favourites'
    },
    {
      icon: '⚙️',
      label: 'Settings'
    }
  ];

  return (
    <aside className={`sidebar ${isOpen ? 'open' : 'closed'}`}>
      <nav className="sidebar-nav">
        {menuItems.map((item, index) => (
          <div key={index} className="sidebar-item">
            <button className="sidebar-btn">
              <div className="sidebar-icon-wrapper">
                <span className="sidebar-icon">{item.icon}</span>
              </div>
              <div className="sidebar-content">
                <h3>{item.label}</h3>
              </div>
            </button>
          </div>
        ))}
      </nav>
    </aside>
  );
}