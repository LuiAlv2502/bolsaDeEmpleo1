import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiGet, apiPost, fmtSalario } from '../../api';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

const NIVELES_ROWS = [0, 1, 2, 3, 4];

export default function EmpresaPuestos() {
    const navigate = useNavigate();
    const [puestos, setPuestos] = useState([]);
    const [caracteristicas, setCaracteristicas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [msg, setMsg] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [form, setForm] = useState({
        descripcion: '', salario: '', publica: 'true', moneda: 'CRC',
        filas: NIVELES_ROWS.map(() => ({ caracId: '', nivel: '1' }))
    });

    useEffect(() => {
        apiGet('/api/empresa/puestos').then(({ ok, data }) => {
            if (!ok) { navigate('/login'); return; }
            setPuestos(data.puestos || []);
            setCaracteristicas(data.caracteristicas || []);
            setLoading(false);
        });
    }, [navigate]);

    if (loading) return <Spinner />;

    const setFila = (i, campo, val) => {
        const filas = form.filas.map((f, idx) => idx === i ? { ...f, [campo]: val } : f);
        setForm({ ...form, filas });
    };

    const publicar = async (e) => {
        e.preventDefault();
        const ids = [], niveles = [];
        form.filas.forEach(f => { if (f.caracId) { ids.push(f.caracId); niveles.push(f.nivel); } });
        setSubmitting(true);
        const { ok, data } = await apiPost('/api/empresa/publicarPuesto', {
            descripcion: form.descripcion, salario: form.salario,
            publica: form.publica, moneda: form.moneda,
            caracteristicaIds: ids, niveles
        });
        setSubmitting(false);
        if (!ok) { setMsg({ tipo: 'error', texto: data.error || 'Error al publicar.' }); return; }
        setPuestos(prev => [data, ...prev]);
        setMsg({ tipo: 'success', texto: 'Puesto publicado correctamente.' });
        setForm({ descripcion: '', salario: '', publica: 'true', moneda: 'CRC',
                  filas: NIVELES_ROWS.map(() => ({ caracId: '', nivel: '1' })) });
    };

    const desactivar = async (id) => {
        await apiPost(`/api/empresa/puestos/${id}/desactivar`);
        setPuestos(prev => prev.map(p => p.id === id ? { ...p, activo: false } : p));
    };

    return (
        <div className="panel-main">
            <div className="panel-header"><h2>Mis Puestos</h2></div>
            <Alert tipo={msg?.tipo} onClose={() => setMsg(null)}>{msg?.texto}</Alert>

            <div className="section-card">
                <h3>Publicar Nuevo Puesto</h3>
                <form className="auth-form" onSubmit={publicar}>
                    <div className="form-group">
                        <label>Descripción general del puesto</label>
                        <textarea rows="1" style={{ resize: 'none' }} value={form.descripcion}
                                  onChange={e => setForm({ ...form, descripcion: e.target.value })}
                                  placeholder="Inserte el nombre del puesto." required />
                    </div>
                    <div className="form-row">
                        <div className="form-group" style={{ flex: 2 }}>
                            <label>Salario ofrecido</label>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                                <input type="number" min="0" step="0.01" value={form.salario}
                                       onChange={e => setForm({ ...form, salario: e.target.value })}
                                       placeholder="Ej: 850000" required style={{ flex: 1, minWidth: 120 }} />
                                <input type="radio" className="moneda-radio" id="moneda-crc" name="moneda" value="CRC"
                                       checked={form.moneda === 'CRC'} onChange={() => setForm({ ...form, moneda: 'CRC' })} />
                                <label htmlFor="moneda-crc" className="btn-moneda">₡ Colones</label>
                                <input type="radio" className="moneda-radio" id="moneda-usd" name="moneda" value="USD"
                                       checked={form.moneda === 'USD'} onChange={() => setForm({ ...form, moneda: 'USD' })} />
                                <label htmlFor="moneda-usd" className="btn-moneda">$ Dólares</label>
                            </div>
                        </div>
                        <div className="form-group">
                            <label>Tipo de publicación</label>
                            <select value={form.publica} onChange={e => setForm({ ...form, publica: e.target.value })}>
                                <option value="true">Pública</option>
                                <option value="false">Privada</option>
                            </select>
                        </div>
                    </div>
                    <div className="form-group">
                        <label>Características requeridas <small style={{ fontWeight: 'normal', color: '#888' }}>(máx. 5)</small></label>
                        {form.filas.map((fila, i) => (
                            <div key={i} className="form-row" style={{ alignItems: 'flex-end', marginBottom: 8 }}>
                                <div className="form-group" style={{ flex: 2 }}>
                                    <select value={fila.caracId} onChange={e => setFila(i, 'caracId', e.target.value)}>
                                        <option value="">Ninguna</option>
                                        {caracteristicas.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
                                    </select>
                                </div>
                                <div className="form-group" style={{ flex: 1 }}>
                                    <input type="number" min="1" max="5" value={fila.nivel}
                                           onChange={e => setFila(i, 'nivel', e.target.value)} />
                                </div>
                            </div>
                        ))}
                    </div>
                    <button type="submit" className="btn-submit" disabled={submitting}>
                        {submitting ? 'Publicando...' : 'Publicar Puesto'}
                    </button>
                </form>
            </div>

            <div className="section-card">
                <h3>Puestos Registrados</h3>
                {puestos.length === 0
                    ? <div className="empty-msg">No tiene puestos publicados aún.</div>
                    : (
                        <table className="panel-table">
                            <thead>
                                <tr><th>#</th><th>Descripción</th><th>Salario</th><th>Tipo</th><th>Estado</th><th>Características</th><th>Acción</th></tr>
                            </thead>
                            <tbody>
                                {puestos.map(p => (
                                    <tr key={p.id}>
                                        <td>{p.id}</td>
                                        <td>{(p.descripcion || '').substring(0, 80)}</td>
                                        <td>{fmtSalario(p.moneda, p.salario)}</td>
                                        <td><span className="badge" style={{ background: p.publica ? '#eaf4fb' : '#fdf2fb', color: p.publica ? '#2980b9' : '#8e44ad' }}>{p.publica ? 'Pública' : 'Privada'}</span></td>
                                        <td><span className="badge" style={{ background: p.activo ? '#eafaf1' : '#fdecea', color: p.activo ? '#1e8449' : '#c0392b' }}>{p.activo ? 'Activo' : 'Inactivo'}</span></td>
                                        <td>{(p.puestoCaracteristicas || []).length ? p.puestoCaracteristicas.map(pc => <span key={pc.caracteristica?.id} className="badge" style={{ marginRight: 4 }}>{pc.caracteristica?.nombre} (Nv.{pc.nivelRequerido})</span>) : '—'}</td>
                                        <td>
                                            <Link to={`/empresa/puestos/${p.id}`} className="btn-detalle">Ver detalles</Link>
                                            {p.activo && <button className="btn-desactivar" onClick={() => desactivar(p.id)}>Desactivar</button>}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )
                }
            </div>
        </div>
    );
}
