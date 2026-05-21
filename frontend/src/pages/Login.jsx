import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiPost } from '../api';
import { useAuth } from '../context/AuthContext';

export default function Login() {
    const [credencial, setCredencial] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

    const onSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const { ok, data } = await apiPost('/api/auth/login', { credencial, password });
        if (!ok) { setError(data.error || 'Error al iniciar sesión.'); return; }
        login({ tipo: data.tipo, nombre: data.nombre, id: data.id });
        if (data.tipo === 'admin')        navigate('/admin/panel');
        else if (data.tipo === 'empresa') navigate('/empresa/dashboard');
        else                              navigate('/oferente/dashboard');
    };

    return (
        <main className="auth-main">
            <div className="auth-card">
                <div className="auth-header">
                    <h2>Iniciar Sesión</h2>
                    <p>Ingresa tus credenciales para entrar</p>
                </div>
                {error && <div className="alert alert-error">{error}</div>}
                <form className="auth-form" onSubmit={onSubmit}>
                    <div className="form-group">
                        <label>Correo electrónico o identificación</label>
                        <input type="text" value={credencial}
                               onChange={e => setCredencial(e.target.value)}
                               placeholder="tu@correo.com o número de identificación"
                               required autoComplete="username" />
                    </div>
                    <div className="form-group">
                        <label>Contraseña</label>
                        <input type="password" value={password}
                               onChange={e => setPassword(e.target.value)}
                               placeholder="••••••••" required autoComplete="current-password" />
                    </div>
                    <button type="submit" className="btn-submit">Ingresar</button>
                </form>
                <div className="auth-footer">
                    <p>¿No tiene cuenta?{' '}
                        <a href="#/empresa/registro">Registrar empresa</a> ·{' '}
                        <a href="#/oferente/registro">Registrarse como Oferente</a>
                    </p>
                    <p><a href="#/">← Volver al inicio</a></p>
                </div>
            </div>
        </main>
    );
}

