import { useNavigate } from 'react-router-dom';
import { useTour } from './Tour/TourContext.jsx';

export default function Sidebar({ isOpen, onClose }) {
  const navigate = useNavigate();
  const { startTour } = useTour();

  const handleStartTour = () => {
    // Close sidebar on mobile/all devices when starting tour
    if (onClose) {
      onClose();
    }
    // Small delay to let sidebar close animation complete
    setTimeout(() => {
      startTour();
    }, 100);
  };
  
  const menuItems = [
    { icon: '📈', label: 'Insights', path: '/home' },
    { icon: '⭐️', label: 'Favourites', path: '/favourites', dataTour: 'sidebar-favourites' },
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
              data-tour={item.dataTour}
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
        
        {/* Tour button */}
        {/* <div className="sidebar-item" style={{ marginTop: 'auto' }}>
          <button
            type="button"
            className="sidebar-btn"
            onClick={startTour}
            style={{ opacity: 0.8 }}
          >
            <div className="sidebar-icon-wrapper">
              <span className="sidebar-icon">🎓</span>
            </div>
            <div className="sidebar-content">
              <h3>Take Tour</h3>
            </div>
          </button>
        </div> */}
      </nav>
    </aside>
  );
}