import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiPost } from '../api';

export default function Navbar() {
    const { usuario, logout } = useAuth();
    const navigate = useNavigate();

    const cerrarSesion = async (e) => {
        e.preventDefault();
        await apiPost('/api/auth/logout');
        logout();
        navigate('/');
    };

    if (!usuario) return (
        <nav>
            <Link to="/">Inicio</Link>
            <Link to="/login">Iniciar Sesión</Link>
            <Link to="/empresa/registro">Registrar Empresa</Link>
            <Link to="/oferente/registro">Registrar Oferente</Link>
        </nav>
    );

    return (
        <nav>
            <Link to="/">Inicio</Link>
            {usuario.tipo === 'empresa'  && <Link to="/empresa/dashboard">Dashboard</Link>}
            {usuario.tipo === 'oferente' && <Link to="/oferente/dashboard">Dashboard</Link>}
            {usuario.tipo === 'admin'    && <Link to="/admin/panel">Panel Admin</Link>}
            <span>Bienvenido, <strong>{usuario.nombre}</strong></span>
            <a href="#" onClick={cerrarSesion}>Cerrar Sesión</a>
        </nav>
    );
}

