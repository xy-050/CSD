import { useState } from 'react'

export default function SignupPage({ setCurrentPage, setUser, users, setUsers }) {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')

  const onSubmit = (e) => {
    e.preventDefault()
    if (password !== confirm) return setError('Passwords do not match.')

    const exists = users.some(
      u => u.username.toLowerCase() === username.toLowerCase() || u.email.toLowerCase() === email.toLowerCase()
    )
    if (exists) return setError('User with that username/email already exists.')

    const newUser = { username, email, password }
    setUsers([...users, newUser])
    setUser(newUser)
    setCurrentPage('home')
  }

  return (
    <div className="signup-container">
      <div className="signup-box">
        <div className="signup-header">
          <h1>Create account</h1>
          <p>Join us on this Tariff-ic day! 🌤️</p>
        </div>

        {error && <div className="error-message mb-3">{error}</div>}

        <form className="form-container" onSubmit={onSubmit}>
          <div className="input-group">
            <label>Username</label>
            <div className="input-wrapper">
              <span className="input-icon">👤</span>
              <input
                placeholder="yourname"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label>Email</label>
            <div className="input-wrapper">
              <span className="input-icon">📧</span>
              <input
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label>Password</label>
            <div className="input-wrapper">
              <span className="input-icon">🔒</span>
              <input
                type="password"
                placeholder="At least 8 characters"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={8}
              />
            </div>
          </div>

          <div className="input-group">
            <label>Confirm password</label>
            <div className="input-wrapper">
              <span className="input-icon">✅</span>
              <input
                type="password"
                placeholder="Repeat password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                required
              />
            </div>
          </div>

          <button className="submit-btn signup-btn" type="submit">Sign up</button>
        </form>

        <div className="switch-form">
          <p>
            Already have an account?{' '}
            <button className="link-btn" onClick={() => setCurrentPage('login')}>
              Log in
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}