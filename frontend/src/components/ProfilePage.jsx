import { useState } from "react"

export default function ProfilePage({ user, setUser, setCurrentPage }) {
  const [form, setForm] = useState({
    username: user?.username || "",
    email: user?.email || ""
  })
  const [msg, setMsg] = useState("")

  const update = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const save = (e) => {
    e.preventDefault()
    setUser((prev) => ({ ...prev, username: form.username.trim(), email: form.email.trim() }))
    setMsg("Profile updated")
    setTimeout(() => setCurrentPage("home"), 500)
  }

  return (
    <div className="homepage">
      <main className="main-content">
        <section className="hero-section">
          <h1 className="hero-title">Your profile</h1>
          <p className="hero-subtitle">Update your account details.</p>
        </section>

        <section className="account-info" id="account">
          <h2>Account</h2>

          <form className="form-container mt-3" onSubmit={save}>
            <div className="input-group">
              <label>Username</label>
              <input
                className="search-input" /* reuse your clean input style */
                value={form.username}
                onChange={update("username")}
                placeholder="Enter a username"
              />
            </div>

            <div className="input-group">
              <label>Email</label>
              <input
                className="search-input"
                value={form.email}
                onChange={update("email")}
                placeholder="name@example.com"
              />
            </div>

            <div style={{ display: "flex", gap: "0.75rem", marginTop: "0.5rem" }}>
              <button type="submit" className="feature-btn">Save changes</button>
              <button
                type="button"
                className="feature-btn"
                onClick={() => setCurrentPage("home")}
                style={{ background: "linear-gradient(135deg, var(--brand-strong), var(--brand-ink))" }}
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