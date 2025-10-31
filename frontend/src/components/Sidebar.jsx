import { useNavigate } from 'react-router-dom';

export default function Sidebar({ isOpen }) {
  const navigate = useNavigate();

  const menuItems = [
    { icon: '📈', label: 'Insights', path: '/home' },
    { icon: '⭐️', label: 'Favourites', path: '/favourites' },
    { icon: '⚙️', label: 'Settings', path: '/profile' }
  ];

  return (
    <aside className={`sidebar ${isOpen ? 'open' : 'closed'}`}>
      <nav className="sidebar-nav">
        {menuItems.map((item, index) => (
          <div key={index} className="sidebar-item">
            <button
              type="button"
              className="sidebar-btn"
              onClick={() => navigate(item.path)}
            >
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
