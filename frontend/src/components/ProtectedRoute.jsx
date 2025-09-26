import { Navigate } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/AxiosConfig.jsx";

export default function PrivateRoute({ children }) {
    const [isAuth, setIsAuth] = useState(null);

    useEffect(() => {
        api.get("/authStatus")
            .then((res) => {
                if (res.status == 200) {
                    setIsAuth(true);
                }
            })
            .catch(() => setIsAuth(false))
    }, []);

    if (isAuth == null ) {
        return <div>Loading...</div>;
    }

    return isAuth ? children : <Navigate to="/login" />;
}