import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar.jsx";
import Sidebar from "./Sidebar.jsx";
import api from "../api/AxiosConfig.jsx";
import Popup from "./Popup/Popup.jsx";

export default function ProfilePage({ }) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [userID, setUserID] = useState("");
  const [msg, setMsg] = useState("");
  const [showPopup, setShowPopup] = useState(false);
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
      const handleUpdate = async (e) => {
        e.preventDefault();

        try {
          const response = await api.post(`/updateUser/${encodeURIComponent(userID)}`, {
            "userID": userID,
            "username": username,
            "email": email
          });
          console.log(response);
        } catch (error) {
          console.log("Error: " + error.response.data);

          if (error.response.data.trim() === "Nothing to update.") {
            setMsg("Nothing to update. Redirecting to home...");
            await new Promise(resolve => setTimeout(resolve, 2000));
            navigate("/");
            return;
          }

          setMsg(error.response.data);
          return;
        }

        setMsg("Update complete! Re-directing you back to login...");
        await new Promise(resolve => setTimeout(resolve, 2000));
        localStorage.removeItem("token");
        navigate("/login");
      }
    };

    const handleCancel = () => {
      navigate("/");
    };

    const handleChangePassword = () => {
      navigate("/change_password");
    }

    const handleDelete = () => {
      setShowPopup(true);
    }

    const handleConfirmDelete = async (e) => {
      e.preventDefault();
      setShowPopup(false);

      try {
        const response = await api.delete(`/account/${encodeURIComponent(userID)}`);
        console.log(response);
        localStorage.removeItem("token");
        navigate("/login");
      } catch (error) {
        console.log("Error " + error);
        console.log(response.error.data);
        setMsg("Error deleting account. Please try again.")
      }
    }

    const handleCancelDelete = () => {
      setShowPopup(false);
    }

    return (
      <div className="homepage">
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
                  <label>Username</label>
                  <input
                    className="search-input"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="name@example.com"
                  />
                </div>
                <div className="input-group">
                  <label>Email</label>
                  <input
                    className="search-input"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="name@example.com"
                  />
                </div>

                {msg && <p className="mt-2" style={{ color: "var(--brand-ink)" }}>{msg}</p>}

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

                  <button
                    type="button"
                    className="feature-btn"
                    style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
                    onClick={handleChangePassword}
                  >
                    Change Password
                  </button>
                  <button
                    type="button"
                    className="feature-btn"
                    style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
                    onClick={handleDelete}
                  >
                    Delete Account
                  </button>

                  {
                    showPopup && (
                      <Popup onClose={handleConfirmDelete}>
                        <h2>Confirmation</h2>
                        <p>This action is irreversible. Do you want to continue?</p>
                        <div style={{ display: "flex", gap: "0.75rem", marginTop: "0.5rem" }}>
                          <button
                            type="button"
                            className="feature-btn"
                            style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
                            onClick={handleConfirmDelete}
                          >
                            Yes, Delete
                          </button>
                          <button
                            type="button"
                            className="feature-btn"
                            style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
                            onClick={handleCancelDelete}
                          >
                            Cancel
                          </button>
                        </div>
                      </Popup>
                    )
                  }

                </div>
              </form>
            </section>
          </main>
        </div>
      </div>
    );
  }
}
