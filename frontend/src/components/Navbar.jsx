import { useNavigate } from 'react-router-dom';
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
            <a href="#/">Inicio</a>
            <a href="#/login">Iniciar Sesión</a>
            <a href="#/empresa/registro">Registrar Empresa</a>
            <a href="#/oferente/registro">Registrar Oferente</a>
        </nav>
    );

    return (
        <nav>
            <a href="#/">Inicio</a>
            {usuario.tipo === 'empresa'  && <a href="#/empresa/dashboard">Dashboard</a>}
            {usuario.tipo === 'oferente' && <a href="#/oferente/dashboard">Dashboard</a>}
            {usuario.tipo === 'admin'    && <a href="#/admin/panel">Panel Admin</a>}
            <span>Bienvenido, <strong>{usuario.nombre}</strong></span>
            <a href="#" onClick={cerrarSesion}>Cerrar Sesión</a>
        </nav>
    );
}

