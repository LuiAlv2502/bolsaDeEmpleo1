import { Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function EmpresaDashboard() {
    const { loading, error } = useApi('/api/empresa/dashboard');

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
                    <h2>Panel de Empresa</h2>
                    <p>Gestione sus puestos y candidatos</p>
                </div>
                <div className="registro-section" style={{ marginTop: 0 }}>
                    <div className="registro-card">
                        <h3>Mis Puestos</h3>
                        <p>Publique nuevos puestos y gestione los ya existentes</p>
                        <Link to="/empresa/puestos" className="btn btn-secondary">Ver puestos</Link>
                    </div>
                </div>
            </div>
        </main>
    );
}
