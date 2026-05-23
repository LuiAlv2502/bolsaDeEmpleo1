import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Protege rutas que requieren autenticación.
 * roles: array de tipos permitidos, ej. ['empresa'] o ['admin']
 * Si no se pasa roles, solo verifica que haya sesión activa.
 */
export default function ProtectedRoute({ children, roles }) {
    const { usuario } = useAuth();

    if (!usuario) {
        return <Navigate to="/login" replace />;
    }

    if (roles && !roles.includes(usuario.tipo)) {
        // Redirige al dashboard propio si intenta acceder a una ruta no permitida
        const destino =
            usuario.tipo === 'admin'    ? '/admin/panel' :
            usuario.tipo === 'empresa'  ? '/empresa/dashboard' :
                                          '/oferente/dashboard';
        return <Navigate to={destino} replace />;
    }

    return children;
}

