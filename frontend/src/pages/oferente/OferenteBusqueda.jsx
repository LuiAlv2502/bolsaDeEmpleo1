import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiGet, fmtSalario, fmtFecha } from '../../api';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function OferenteBusqueda() {
    const navigate = useNavigate();
    const [resultados, setResultados] = useState([]);

    const [caracteristicas, setCaracteristicas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [tipo, setTipo] = useState('todos');
    const [palabra, setPalabra] = useState('');
    const [salarioMin, setSalarioMin] = useState('');
    const [caracId, setCaracId] = useState('');

    const buscar = useCallback(async () => {
        setLoading(true);
        setError(null);
        const params = new URLSearchParams({ tipo });
        if (palabra) params.append('palabra', palabra);
        if (salarioMin) params.append('salarioMin', salarioMin);
        if (caracId) params.append('caracteristica', caracId);

        const { ok, data } = await apiGet(`/api/oferente/puestos/buscar?${params}`);
        if (!ok) {
            if (data?.error?.includes('autorizado') || ok === false && !data?.error) {
                navigate('/login');
                return;
            }
            setError(data?.error || 'Error al buscar puestos.');
        } else {
            setResultados(data.resultados || []);
            setCaracteristicas(data.caracteristicas || []);
        }
        setLoading(false);
    }, [tipo, palabra, salarioMin, caracId, navigate]);

    useEffect(() => { buscar(); }, [tipo]);  // recarga al cambiar tipo

    function handleBuscar(e) {
        e.preventDefault();
        buscar();
    }

    if (loading) return <Spinner />;

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Buscar Puestos</h2>
                    <p>Explore los puestos disponibles según su perfil</p>
                </div>

                {error && <Alert tipo="error">{error}</Alert>}

                {/* Filtros */}
                <form className="auth-form" onSubmit={handleBuscar} style={{ marginBottom: 20 }}>
                    <div className="form-row">
                        <div className="form-group" style={{ flex: 2 }}>
                            <label>Tipo</label>
                            <select value={tipo} onChange={e => setTipo(e.target.value)}>
                                <option value="todos">Todos los puestos</option>
                                <option value="publica">Puestos públicos</option>
                                <option value="privada">Puestos privados</option>
                            </select>
                        </div>
                        <div className="form-group" style={{ flex: 3 }}>
                            <label>Palabra clave</label>
                            <input
                                type="text"
                                className="form-input"
                                placeholder="Ej: desarrollador, ventas..."
                                value={palabra}
                                onChange={e => setPalabra(e.target.value)}
                            />
                        </div>
                        <div className="form-group" style={{ flex: 2 }}>
                            <label>Salario mínimo</label>
                            <input
                                type="number"
                                min="0"
                                step="1"
                                className="form-input"
                                placeholder="Ej: 500000"
                                value={salarioMin}
                                onChange={e => setSalarioMin(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="form-row">
                        <div className="form-group" style={{ flex: 3 }}>
                            <label>Característica requerida</label>
                            <select value={caracId} onChange={e => setCaracId(e.target.value)}>
                                <option value="">Todas</option>
                                {caracteristicas.map(c => (
                                    <option key={c.id} value={c.id}>
                                        {c.parent ? `${c.parent.nombre} > ${c.nombre}` : c.nombre}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group" style={{ flex: 1, justifyContent: 'flex-end', paddingTop: 22 }}>
                            <button type="submit" className="btn-submit" style={{ margin: 0 }}>Buscar</button>
                        </div>
                    </div>
                </form>

                {/* Resultados */}
                <div className="section-card">
                    <h3>Resultados ({resultados.length})</h3>
                    {resultados.length === 0 ? (
                        <div className="empty-msg">No se encontraron puestos con los filtros aplicados.</div>
                    ) : (
                        <table className="panel-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Descripción</th>
                                    <th>Salario</th>
                                    <th>Tipo</th>
                                    <th>Fecha</th>
                                    <th>Características</th>
                                </tr>
                            </thead>
                            <tbody>
                                {resultados.map((p, i) => (
                                    <tr key={p.id}>
                                        <td>{i + 1}</td>
                                        <td>{(p.descripcion || '').substring(0, 80)}</td>
                                        <td>{fmtSalario(p.moneda, p.salario)}</td>
                                        <td>
                                            <span className="badge" style={{ background: p.publica ? '#eaf4fb' : '#fdf2fb', color: p.publica ? '#2980b9' : '#8e44ad' }}>
                                                {p.publica ? 'Pública' : 'Privada'}
                                            </span>
                                        </td>
                                        <td>{fmtFecha(p.fechaPublicacion)}</td>
                                        <td>
                                            {(p.puestoCaracteristicas || []).length
                                                ? p.puestoCaracteristicas.map(pc => (
                                                    <span key={pc.caracteristica?.id} className="badge" style={{ marginRight: 4 }}>
                                                        {pc.caracteristica?.nombre} (Nv.{pc.nivelRequerido})
                                                    </span>
                                                ))
                                                : '—'
                                            }
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

            </div>
        </main>
    );
}
