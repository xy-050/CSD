import { Navigate } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/AxiosConfig.jsx";

export default function PrivateRoute({ children }) {
    const [isAuth, setIsAuth] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            setIsAuth(false);
            return;
        }

        api.get("/verify-token", {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then((res) => {
            if (res.status === 200) {
                setIsAuth(true);
            }
        })
        .catch(() => {
            localStorage.removeItem('token'); // Clear invalid token
            setIsAuth(false);
        });
    }, []);

    if (isAuth === null) {
        return <div>Loading...</div>;
    }

    return isAuth ? children : <Navigate to="/login" />;
}