import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [usuario, setUsuario] = useState(() => {
        const s = sessionStorage.getItem('usuario');
        return s ? JSON.parse(s) : null;
    });

    const login = (data) => {
        // data incluye { token, tipo, nombre, id }
        sessionStorage.setItem('usuario', JSON.stringify(data));
        if (data.token) sessionStorage.setItem('token', data.token);
        setUsuario(data);
    };

    const logout = () => {
        sessionStorage.removeItem('usuario');
        sessionStorage.removeItem('token');
        setUsuario(null);
    };

    return (
        <AuthContext.Provider value={{ usuario, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
