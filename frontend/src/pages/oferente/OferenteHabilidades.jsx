import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiGet, apiPost, apiDelete } from '../../api';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function OferenteHabilidades() {
    const navigate = useNavigate();
    const [habilidades, setHabilidades] = useState([]);
    const [caracteristicas, setCaracteristicas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [msg, setMsg] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [caracId, setCaracId] = useState('');
    const [nivel, setNivel] = useState('1');

    useEffect(() => {
        apiGet('/api/oferente/habilidades').then(({ ok, data }) => {
            if (!ok) { navigate('/login'); return; }
            setHabilidades(data.habilidades || []);
            setCaracteristicas(data.caracteristicas || []);
            setLoading(false);
        });
    }, [navigate]);

    if (loading) return <Spinner />;

    const agregar = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        const { ok, data } = await apiPost('/api/oferente/habilidades/agregar', { caracteristicaId: caracId, nivel });
        setSubmitting(false);
        if (!ok) { setMsg({ tipo: 'error', texto: data.error }); return; }
        setHabilidades(prev => {
            const existe = prev.find(h => String(h.caracteristica?.id) === String(caracId));
            if (existe) return prev.map(h => String(h.caracteristica?.id) === String(caracId) ? { ...h, nivel: parseInt(nivel) } : h);
            const caract = caracteristicas.find(c => String(c.id) === String(caracId));
            return [...prev, { id: data.id, caracteristica: caract, nivel: parseInt(nivel) }];
        });
        setMsg({ tipo: 'success', texto: 'Habilidad guardada correctamente.' });
        setCaracId('');
        setNivel('1');
    };

    const eliminar = async (id) => {
        await apiDelete(`/api/oferente/habilidades/${id}`);
        setHabilidades(prev => prev.filter(h => h.id !== id));
    };

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Mis Habilidades</h2>
                    <p>Agregue o actualice sus habilidades y niveles de dominio.</p>
                </div>
                <Alert tipo={msg?.tipo} onClose={() => setMsg(null)}>{msg?.texto}</Alert>

                <div className="section-card" style={{ marginBottom: 24 }}>
                    <h3>Agregar / actualizar habilidades</h3>
                    <form className="auth-form" onSubmit={agregar}>
                        <div className="form-row">
                            <div className="form-group" style={{ flex: 3 }}>
                                <label>Característica</label>
                                <select value={caracId} onChange={e => setCaracId(e.target.value)} required>
                                    <option value="">Seleccionar</option>
                                    {caracteristicas.map(c => (
                                        <option key={c.id} value={c.id}>
                                            {c.parent ? c.parent.nombre + ' > ' + c.nombre : c.nombre}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group" style={{ flex: 1 }}>
                                <label>Nivel (1-5)</label>
                                <input type="number" min="1" max="5" value={nivel} onChange={e => setNivel(e.target.value)} required />
                            </div>
                            <div className="form-group" style={{ flex: 1, justifyContent: 'flex-end', paddingTop: 22 }}>
                                <button type="submit" className="btn-submit" style={{ margin: 0, padding: '10px 18px' }} disabled={submitting}>
                                    {submitting ? '...' : '+ Agregar'}
                                </button>
                            </div>
                        </div>
                        <p style={{ fontSize: 15, color: '#888', marginTop: -8 }}>
                            Nivel 1 = básico · 3 = intermedio · 5 = experto.
                        </p>
                    </form>
                </div>

                <div className="section-card">
                    <h3>Mis habilidades registradas</h3>
                    {habilidades.length === 0
                        ? <div className="empty-msg">Aún no tiene habilidades registradas.</div>
                        : (
                            <table className="panel-table">
                                <thead><tr><th>Característica</th><th>Categoría padre</th><th>Nivel</th><th>Acción</th></tr></thead>
                                <tbody>
                                    {habilidades.map(h => (
                                        <tr key={h.id}>
                                            <td>{h.caracteristica?.nombre ?? '-'}</td>
                                            <td>{h.caracteristica?.parent ? <span className="badge">{h.caracteristica.parent.nombre}</span> : <span style={{ color: '#aaaaaa' }}>-</span>}</td>
                                            <td><span className="nivel-badge">{h.nivel}</span></td>
                                            <td><button className="btn-eliminar" onClick={() => eliminar(h.id)}>Eliminar</button></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )
                    }
                </div>
                <div className="auth-footer">
                    <p><Link to="/oferente/dashboard">← Volver al menú</Link></p>
                </div>
            </div>
        </main>
    );
}
