import React, { useState } from 'react';
import LoginPage from './components/LoginPage.jsx';
import SignupPage from './components/SignupPage.jsx';
import HomePage from './components/Homepage.jsx';
import ProfilePage from './components/ProfilePage.jsx';
import johnpork from "./assets/johnpork.png";
import SearchResults from './components/SearchResults.jsx';
import CalculatorPage from "./components/CalculatorPage.jsx";
import './App.css';

export default function App() {
  const [currentPage, setCurrentPage] = useState('login');
  const [user, setUser] = useState(null);
  const [users, setUsers] = useState([
    { username: 'demo', email: 'demo@example.com', password: 'password123', avatarUrl: johnpork }
  ]);
  const [results, setResults] = useState([]); // Search results state
  const [calcQuery, setCalcQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false); // Loading state

  const runSearch = async (term) => {
    setIsLoading(true);
    try {
      const response = await fetch(
        `http://localhost:8080/api/tariffs/search?keyword=${term}`,
        {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
        }
      );
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      setResults(data);  // Update results in parent
      setCurrentPage('searchResults');  // Navigate to search results page
    } catch (error) {
      console.error('Error during search:', error);
      setResults([]);  // Optionally clear results on error
    } finally {
      setIsLoading(false);
    }
  };

  // Function to handle login
  const handleLogin = (email, password) => {
    const foundUser = users.find(u => u.email === email && u.password === password);
    if (foundUser) {
      setUser(foundUser);
      setCurrentPage("home");
    } else {
      alert("Invalid credentials");
    }
  };

  // Function to handle logout
  const handleLogout = () => {
    setUser(null);
    setCurrentPage("login");
  };

  const appProps = { currentPage, setCurrentPage, user, setUser, users, setUsers, calcQuery, setCalcQuery };

  switch (currentPage) {
    case "home":
      return user ? (
        <HomePage 
          onSearch={runSearch} // Pass the runSearch function as a prop
          setCurrentPage={setCurrentPage} 
          user={user} 
          {...appProps} // Spread the rest of the app props here
        />
      ) : <LoginPage {...appProps} />;
    
    case "searchResults":
      return (
        <SearchResults 
          results={results} 
          onSelectOption={(option) => {
            setCalcQuery(option.name);
            setCurrentPage("calculator"); // try to change to searchResults???
          }} 
        />
      );

    case "calculator":
      return user ? <CalculatorPage {...appProps} /> : <LoginPage {...appProps} />;
    case "profile":
      return user ? <ProfilePage {...appProps} /> : <LoginPage {...appProps} />;
    case "signup":
      return <SignupPage {...appProps} />;
    case "login":
    default:
      return <LoginPage {...appProps} />;
  }
};

// export default App;
