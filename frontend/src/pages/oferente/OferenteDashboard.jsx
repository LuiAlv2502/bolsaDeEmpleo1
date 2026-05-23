import { Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function OferenteDashboard() {
    const { loading, error } = useApi('/api/oferente/dashboard');

    if (loading) return <Spinner />;
    if (error) return (
        <main className="auth-main">
            <div className="auth-card">
                <Alert tipo="error">{error}</Alert>
                <p style={{ textAlign: 'center', marginTop: 12 }}><Link to="/login">← Ir al login</Link></p>
            </div>
        </main>
    );

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Mi Panel</h2>
                    <p>Gestione su perfil, habilidades y CV</p>
                </div>
                <div className="registro-section" style={{ marginTop: 0 }}>
                    <div className="registro-card">
                        <h3>Mis Habilidades</h3>
                        <p>Agregue o actualice sus habilidades y niveles</p>
                        <Link to="/oferente/habilidades" className="btn btn-secondary">Gestionar habilidades</Link>
                    </div>
                    <div className="registro-card">
                        <h3>Mi Currículum (CV)</h3>
                        <p>Suba su currículum en formato PDF para que las empresas lo puedan encontrar.</p>
                        <Link to="/oferente/cv" className="btn btn-primary">Subir CV</Link>
                    </div>
                </div>
            </div>
        </main>
    );
}
