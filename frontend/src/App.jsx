import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import ProtectedRoute from "./components/ProtectedRoute.jsx"
import LoginPage from './components/LoginPage.jsx';
import SignupPage from './components/SignupPage.jsx';
import HomePage from './components/Homepage.jsx';
import ProfilePage from './components/ProfilePage.jsx';
import SearchResults from './components/SearchResults.jsx';
import CalculatorPage from "./components/CalculatorPage.jsx";
import './App.css';

export default function App() {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
                <Route path="/home" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
                <Route path="/results" element={<ProtectedRoute><SearchResults /></ProtectedRoute>} />
                <Route path="/calculator" element={<ProtectedRoute><CalculatorPage /></ProtectedRoute>} />
                <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    )
}

// // Create a wrapper component to handle navigation
// function AppContent() {
//   const navigate = useNavigate();
//   const [user, setUser] = useState(null);
//   const [users, setUsers] = useState([
//     { username: 'demo', email: 'demo@example.com', password: 'password123', avatarUrl: johnpork }
//   ]);
//   const [results, setResults] = useState([]);
//   const [calcQuery, setCalcQuery] = useState("");
//   const [isLoading, setIsLoading] = useState(false);

//   const runSearch = async (term) => {
//     setIsLoading(true);
//     try {
//       const response = await fetch(
//         `http://localhost:8080/api/tariffs/search?keyword=${encodeURIComponent(term)}`,
//         {
//           method: 'GET',
//           headers: { 'Content-Type': 'application/json' },
//         }
//       );

//       if (!response.ok) {
//         throw new Error(`HTTP error! status: ${response.status}`);
//       }

//       const data = await response.json();
//       console.log('Search results:', data);
//       setResults(data);
//       navigate('/results');
//     } catch (error) {
//       console.error('Error during search:', error);
//       setResults([]);
//     } finally {
//       setIsLoading(false);
//     }
//   };

//   // Function to handle login
//   const handleLogin = async (email, password) => {
//     try {
//       const response = await fetch('http://localhost:8080/login', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({ username: email, password: password }),
//       });

//       if (response.ok) {
//         // Backend login successful - find user in local array or create one
//         const foundUser = users.find(u =>
//           (u.email === emailOrUsername || u.username === emailOrUsername) &&
//           u.password === password
//         );
//         setUser(foundUser || { email: emailOrUsername, username: emailOrUsername });
//         navigate('/home');
//         return;
//       }
//     } catch (error) {
//       console.error('Backend login failed, trying local:', error);
//     }
//   };

//   // Function to handle logout
//   const handleLogout = () => {
//     setUser(null);
//     navigate('/login');
//   };

//   const handleSelectOption = (option) => {
//     setCalcQuery(option.htsno || option.description);
//     navigate('/calculator');
//   };

//   // Protected route wrapper
//   const ProtectedRoute = ({ children }) => {
//     return user ? children : <Navigate to="/login" replace />;
//   };

//   // const appProps = { currentPage, setCurrentPage, user, setUser, users, setUsers, calcQuery, setCalcQuery };

//   return (
//     <Routes>
//       <Route
//         path="/login"
//         element={
//           <LoginPage
//             handleLogin={handleLogin}
//             users={users}
//             setUser={setUser}
//           />
//         }
//       />
//       <Route
//         path="/signup"
//         element={
//           <SignupPage
//             users={users}
//             setUsers={setUsers}
//             setUser={setUser}
//           />
//         }
//       />
//       <Route
//         path="/home"
//         element={
//           <ProtectedRoute>
//             <HomePage
//               onSearch={runSearch}
//               navigate={navigate}
//               user={user}
//               handleLogout={handleLogout}
//               isLoading={isLoading}
//             />
//           </ProtectedRoute>
//         }
//       />
//       <Route
//         path="/results"
//         element={
//           <ProtectedRoute>
//             <SearchResults
//               results={results}
//               onSelectOption={handleSelectOption}
//               user={user}
//               navigate={navigate}
//             />
//           </ProtectedRoute>
//         }
//       />
//       <Route
//         path="/calculator"
//         element={
//           <ProtectedRoute>
//             <CalculatorPage
//               calcQuery={calcQuery}
//               setCalcQuery={setCalcQuery}
//               navigate={navigate}
//               user={user}
//             />
//           </ProtectedRoute>
//         }
//       />
//       <Route
//         path="/profile"
//         element={
//           <ProtectedRoute>
//             <ProfilePage
//               user={user}
//               setUser={setUser}
//               navigate={navigate}
//             />
//           </ProtectedRoute>
//         }
//       />
//       <Route path="/" element={<Navigate to="/login" replace />} />
//     </Routes>
//   );
// }

// export default function App() {
//   return (
//     <Router>
//       <AppContent />
//     </Router>
//   );
// }
