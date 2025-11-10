import axios from 'axios';

// Use localhost during local development, but use relative URLs in production so
// the browser requests go to the same origin (and the frontend nginx can pro
// xy them to the backend service). This avoids calling the user's machine's
// localhost when the app is deployed inside Docker on a remote host.
const isLocalhost = typeof window !== 'undefined' && window.location.hostname === 'localhost'; 
// In production the frontend nginx proxies API requests under /api -> backend
// so use '/api' as the relative base path. During local development talk to
// the backend on localhost:8080 (Dev mode).
const api = axios.create({
    baseURL: isLocalhost ? 'http://localhost:8080' : '/api',
    headers: {"ngrok-skip-browser-warning": "true"},
});

console.log("Axios baseURL:", isLocalhost ? 'http:localhost:8080' : 'api');

// Add a request interceptor
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;