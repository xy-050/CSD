import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom";
import NavBar from "./NavBar.jsx"
import api from "../api/AxiosConfig.jsx";

export default function ProfilePage({ }) {
    // const [username, setUsername] = useState(null);
    const [oldUsername, setOldUsername] = useState("");
    const [email, setEmail] = useState("");
    const [userID, setUserID] = useState("");
    const [msg, setMsg] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const getUsername = async () => {
            try {
                const response = await api.get(`/authStatus`);
                console.log(response);
                setOldUsername(response.data);
            } catch (error) {
                console.log(error);
            }
        }
        getUsername();
    }, []);

    useEffect(() => {
        if (oldUsername != "") {
            const getUserID = async () => {
                try {
                    const response = await api.get(`/currentID`, {
                        params: { username: oldUsername }
                    });
                    console.log(response);
                    setUserID(response.data);
                } catch (error) {
                    console.log(error);
                }
            }
            getUserID();
        }
    }, [oldUsername]);

    useEffect(() => {
        if (oldUsername != "" && userID != "") {
            const getEmail = async () => {
                try {
                    const response = await api.get(`/currentEmail`, {
                        params: { username: oldUsername }
                    });
                    console.log(response);
                    setEmail(response.data);
                } catch (error) {
                    console.log(error);
                }
            }
            getEmail();
        }
    }, [oldUsername, userID]);

    const handleUpdate = async (e) => {
        e.preventDefault();
        // try {
        //     const response = await api.post(`/updateUsername/${encodeURIComponent(userID)}`, {
        //         "userID": userID,
        //         "username": username
        //     });
        //     console.log(response);
        // } catch (error) {
        //     console.log("Error: " + error);
        // }

        try {
            const response = await api.post(`/updateEmail/${encodeURIComponent(userID)}`, {
                "userID": userID,
                "email": email
            });
            console.log(response);
        } catch (error) {
            console.log("Error: " + error);
        }
        console.log("test");
        navigate("/");
    }

    const handleCancel = () => {
        navigate("/");
    }

    return (
        <div className="homepage">
            <NavBar />

            <main className="main-content">
                <section className="hero-section">
                    <h1 className="hero-title">Your profile</h1>
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
                            <button type="submit" className="feature-btn">Save changes</button>
                            <button
                                type="button"
                                className="feature-btn"
                                style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
                                onClick={handleCancel}
                            >
                                Cancel
                            </button>
                        </div>

                        {msg && <p className="mt-2" style={{ color: "var(--brand-ink)" }}>{msg}</p>}
                    </form>
                </section>
            </main>
        </div>
    )
}