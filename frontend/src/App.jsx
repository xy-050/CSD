import React, { useState } from 'react';
// import App from './App.js';
import LoginPage from './components/LoginPage.jsx';
import SignupPage from './components/SignupPage.jsx';
import HomePage from './components/Homepage.jsx';
import './App.css';

const App = () => {
  const [currentPage, setCurrentPage] = useState('login');
  const [user, setUser] = useState(null);
  const [users, setUsers] = useState([
    { username: 'demo', email: 'demo@example.com', password: 'password123' }
  ]);

  const appProps = {
    currentPage,
    setCurrentPage,
    user,
    setUser,
    users,
    setUsers
  };

  // Main render logic
  if (user && currentPage === 'home') {
    return <HomePage {...appProps} />;
  } else if (currentPage === 'signup') {
    return <SignupPage {...appProps} />;
  } else {
    return <LoginPage {...appProps} />;
  }
};

export default App;
