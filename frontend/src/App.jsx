import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { TourProvider } from './components/Tour/TourContext.jsx';
import ProtectedRoute from "./components/ProtectedRoute.jsx"
import LoginPage from './components/LoginPage.jsx';
import SignupPage from './components/SignupPage.jsx';
import HomePage from './components/Homepage.jsx';
import ProfilePage from './components/ProfilePage.jsx';
import SearchResults from './components/SearchResults.jsx';
import CalculatorPage from "./components/CalculatorPage.jsx";
import FavouritesPage from './components/FavouritesPage.jsx';
import ChangePasswordPage from './components/ChangePasswordPage.jsx';
import './App.css';

export default function App() {
    return (
        <Router>
            <TourProvider>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
                    <Route path="/home" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
                    <Route path="/results" element={<ProtectedRoute><SearchResults /></ProtectedRoute>} />
                    <Route path="/calculator" element={<ProtectedRoute><CalculatorPage /></ProtectedRoute>} />
                    <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
                    <Route path="/favourites" element={<ProtectedRoute><FavouritesPage /></ProtectedRoute>} />
                    <Route path="/change_password" element={<ProtectedRoute><ChangePasswordPage /></ProtectedRoute>} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </TourProvider>
        </Router>
    )
}