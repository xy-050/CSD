import React, { useState } from 'react';
import LoginPage from './components/LoginPage.jsx';
import SignupPage from './components/SignupPage.jsx';
import HomePage from './components/Homepage.jsx';
import ProfilePage from './components/ProfilePage.jsx';
import johnpork from "./assets/johnpork.png";
import './App.css';

const App = () => {
  const [currentPage, setCurrentPage] = useState('login');
  const [user, setUser] = useState(null);
  const [users, setUsers] = useState([
    { username: 'demo', email: 'demo@example.com', password: 'password123', avatarUrl: johnpork }
  ]);

  const appProps = { currentPage, setCurrentPage, user, setUser, users, setUsers };

  switch (currentPage) {
    case "home":
      return user ? <HomePage {...appProps} /> : <LoginPage {...appProps} />;
    case "profile":
      return user ? <ProfilePage {...appProps} /> : <LoginPage {...appProps} />;
    case "signup":
      return <SignupPage {...appProps} />;
    case "login":
    default:
      return <LoginPage {...appProps} />;
  }
};

export default App;
