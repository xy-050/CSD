import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'

export default function SignupPage({ setUser, users, setUsers }) {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')

  const onSubmit = (e) => {
    e.preventDefault()
    setError('') // Clear previous errors
    
    // Validation checks
    if (!username.trim()) {
      setError('Username is required.')
      return
    }
    
    if (!email.trim()) {
      setError('Email is required.')
      return
    }
    
    if (!password) {
      setError('Password is required.')
      return
    }
    
    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    
    if (password !== confirm) {
      setError('Passwords do not match.')
      return
    }

    // Check if user already exists
    const exists = users.some(
      u => u.username.toLowerCase() === username.toLowerCase() || 
           u.email.toLowerCase() === email.toLowerCase()
    )
    
    if (exists) {
      setError('User with that username/email already exists.')
      return
    }

    // Create new user
    const newUser = { username: username.trim(), email: email.trim(), password }
    setUsers([...users, newUser])
    setUser(newUser)
  }

  return (
    <div className="signup-container">
      <div className="signup-box">
        <div className="signup-header">
          <h1>Create account</h1>
          <p>Join us on this Tariff-ic day! 🌤️</p>
        </div>

        {error && (
          <div className="error-message mb-3" style={{ 
            backgroundColor: '#fee', 
            color: '#c33', 
            padding: '12px', 
            borderRadius: '8px',
            border: '1px solid #fcc',
            marginBottom: '16px'
          }}>
            {error}
          </div>
        )}

        <form className="form-container" onSubmit={onSubmit}>
          <div className="input-group">
            <label>Username</label>
            <div className="input-wrapper">
              <span className="input-icon">👤</span>
              <input
                type="text"
                placeholder="yourname"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value)
                  if (error) setError('') // Clear error when user types
                }}
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
                onChange={(e) => {
                  setEmail(e.target.value)
                  if (error) setError('')
                }}
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
                onChange={(e) => {
                  setPassword(e.target.value)
                  if (error) setError('')
                }}
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
                onChange={(e) => {
                  setConfirm(e.target.value)
                  if (error) setError('')
                }}
                required
              />
            </div>
          </div>

          <button className="submit-btn signup-btn" type="submit">
            SIGN UP
          </button>
        </form>

        <div className="switch-form">
          <p>
            Already have an account?{' '}
            <Link to="/login" className="link-btn">
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}