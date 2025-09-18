import { useState } from 'react'

export default function LoginPage({ setCurrentPage, setUser, users }) {
  const [emailOrUser, setEmailOrUser] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const onSubmit = (e) => {
    e.preventDefault()
    const found = users.find(
      u =>
        u.username.toLowerCase() === emailOrUser.toLowerCase() ||
        u.email.toLowerCase() === emailOrUser.toLowerCase()
    )
    if (!found || found.password !== password) {
      setError('Invalid credentials. Try demo / password123.')
      return
    }
    setUser(found)
    setCurrentPage('home')
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>Welcome back</h1>
          <p>Sign in to continue</p>
        </div>

        {error && <div className="error-message mb-3">{error}</div>}

        <form className="form-container" onSubmit={onSubmit}>
          <div className="input-group">
            <label>Username or Email</label>
            <div className="input-wrapper">
              <span className="input-icon">🪪</span>
              <input
                placeholder="demo or demo@example.com"
                value={emailOrUser}
                onChange={(e) => setEmailOrUser(e.target.value)}
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
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button className="submit-btn login-btn" type="submit">Sign in</button>
        </form>

        <div className="switch-form">
          <p>
            New here?{' '}
            <button className="link-btn" onClick={() => setCurrentPage('signup')}>
              Create an account
            </button>
          </p>
        </div>

        <div className="demo-info">
          <p>Demo: <b>demo</b> / <b>password123</b></p>
        </div>
      </div>
    </div>
  )
}