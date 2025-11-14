import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import { TourProvider } from './contexts/TourContext.jsx';

import ProtectedRoute from "./guards/ProtectedRoute.jsx"

import LoginPage from './pages/LoginPage.jsx';
import SignupPage from './pages/SignupPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import HomePage from './pages/Homepage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import SearchResults from './pages/SearchResults.jsx';
import CalculatorPage from "./pages/CalculatorPage.jsx";
import FavouritesPage from './pages/FavouritesPage.jsx';
import ChangePasswordPage from './pages/ChangePasswordPage.jsx';
import ResetPasswordPage from "./pages/ResetPasswordPage.jsx";

import './App.css';
import 'leaflet/dist/leaflet.css';

export default function App() {
    return (
        <Router>
            <TourProvider>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
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