import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function LoginPage({setUser, users, handleLogin }) {
  const navigate = useNavigate()
  const [emailOrUser, setEmailOrUser] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const onSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    // Check if users array exists and has data
    if (!users || users.length === 0) {
      setError('No users found. Please try again later.')
      setLoading(false)
      return
    }

    try {
      // If handleLogin function is passed (for backend integration), use it
      if (handleLogin) {
        await handleLogin(emailOrUser, password)
      } else {
        // Otherwise use the local user validation
        const found = users.find(
          u =>
            u.username.toLowerCase() === emailOrUser.toLowerCase() ||
            u.email.toLowerCase() === emailOrUser.toLowerCase()
        )
        
        if (!found || found.password !== password) {
          setError('Invalid credentials. Try demo / password123.')
          setLoading(false)
          return
        }
        
        setUser(found)
        navigate('/home')
      }
    } catch (error) {
      setError('Login failed. Please try again.')
      console.error('Login error:', error)
    }
    
    setLoading(false)
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
                onChange={(e) => {
                  setEmailOrUser(e.target.value)
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
                placeholder="••••••••"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value)
                  if (error) setError('')
                }}
                required
              />
            </div>
          </div>

          <button 
            className="submit-btn login-btn" 
            type="submit"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="switch-form">
          <p>
            New here?{' '}
            <Link to="/signup" className="link-btn">
              Create an account
            </Link>
          </p>
        </div>

        <div className="demo-info">
          <p>Demo: <b>demo</b> / <b>password123</b></p>
        </div>
      </div>
    </div>
  )
}