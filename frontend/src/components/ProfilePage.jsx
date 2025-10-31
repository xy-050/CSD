import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar.jsx";
import Sidebar from "./Sidebar.jsx";
import api from "../api/AxiosConfig.jsx";

export default function ProfilePage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [userID, setUserID] = useState("");
  const [msg, setMsg] = useState("");
  const navigate = useNavigate();

  // sidebar state
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const toggleSidebar = () => setSidebarOpen(v => !v);
  const closeSidebar = () => setSidebarOpen(false);

  // fetch user details
  useEffect(() => {
    const getUserDetails = async () => {
      try {
        const response = await api.get("/currentUserDetails");
        console.log(response);
        setUsername(response.data.username);
        setUserID(response.data.userId);
        setEmail(response.data.email);
      } catch (error) {
        console.log(error);
      }
    };
    getUserDetails();
  }, []);

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post(`/updateEmail/${encodeURIComponent(userID)}`, {
        userID,
        email,
      });
      console.log(response);
      setMsg("✅ Profile updated successfully!");
      setTimeout(() => navigate("/"), 1000);
    } catch (error) {
      console.log("Error: " + error);
      setMsg("❌ Failed to update. Please try again.");
    }
  };

  const handleCancel = () => {
    navigate("/");
  };

  return (
    <div className="homepage">
      {/* Navbar with sidebar toggle */}
      <NavBar onToggleSidebar={toggleSidebar} sidebarOpen={sidebarOpen} />

      <div className="homepage-container">
        {/* Sidebar */}
        <Sidebar isOpen={sidebarOpen} />

        {/* Main Content */}
        <main className="main-content" onClick={closeSidebar}>
          <section className="hero-section">
            <h1 className="hero-title">Your Profile</h1>
            <p className="hero-subtitle">Update your account details.</p>
          </section>

          <section className="account-info" id="account">
            <h2>Account</h2>

            <form className="form-container mt-3" onSubmit={handleUpdate}>
              <div className="input-group">
                <label>Email</label>
                <input
                  className="search-input"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                />
              </div>

              <div style={{ display: "flex", gap: "0.75rem", marginTop: "0.5rem" }}>
                <button type="submit" className="feature-btn">
                  Save changes
                </button>
                <button
                  type="button"
                  className="feature-btn"
                  style={{
                    background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))",
                  }}
                  onClick={handleCancel}
                >
                  Cancel
                </button>
              </div>

              {msg && (
                <p className="mt-2" style={{ color: "var(--brand-ink)" }}>
                  {msg}
                </p>
              )}
            </form>
          </section>
        </main>
      </div>
    </div>
  );
}
