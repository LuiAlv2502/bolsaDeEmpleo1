import { useEffect, useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { apiGet, apiPost, apiDelete, fmtSalario, fmtFecha } from '../../api';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import GraficasAdmin from '../../components/GraficasAdmin';

export default function AdminPanel() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const actualId = searchParams.get('actualId') || '';

    const [data, setData] = useState(null);
    const [msg, setMsg] = useState(null);
    const [caracNombre, setCaracNombre] = useState('');
    const [caracPadreId, setCaracPadreId] = useState('');

    useEffect(() => {
        const url = '/api/admin/panel' + (actualId ? `?actualId=${encodeURIComponent(actualId)}` : '');
        apiGet(url).then(({ ok, status, data: d }) => {
            if (!ok) {
                if (status === 401) navigate('/login');
                return;
            }
            setData(d);
            if (actualId) setCaracPadreId(actualId);
        });
    }, [actualId, navigate]);

    if (!data) return <Spinner texto="Cargando panel de administrador..." />;

    const aprobar = async (tipo, id) => {
        const url = tipo === 'empresa' ? `/api/admin/empresa/aprobar/${id}` : `/api/admin/oferente/aprobar/${id}`;
        const { ok, data: d } = await apiPost(url);
        if (ok) {
            if (tipo === 'empresa') setData(prev => ({ ...prev, empresasPendientes: prev.empresasPendientes.filter(e => e.id !== id) }));
            else setData(prev => ({ ...prev, oferentesPendientes: prev.oferentesPendientes.filter(o => o.identificacion !== id) }));
        } else {
            setMsg({ tipo: 'error', texto: d.error });
        }
    };

    const eliminarCaract = async (id) => {
        const { ok, data: d } = await apiDelete(`/api/admin/caracteristica/${id}`);
        if (ok) setData(prev => ({ ...prev, caracteristicas: prev.caracteristicas.filter(c => c.id !== id) }));
        else setMsg({ tipo: 'error', texto: d.error });
    };

    const agregarCaract = async (e) => {
        e.preventDefault();
        const { ok, data: d } = await apiPost('/api/admin/caracteristica/nueva', {
            nombre: caracNombre, padreId: caracPadreId || null
        });
        if (!ok) { setMsg({ tipo: 'error', texto: d.error }); return; }
        setData(prev => ({
            ...prev,
            caracteristicas: [...prev.caracteristicas, d],
            todasCaracteristicas: [...(prev.todasCaracteristicas || []), d]
        }));
        setMsg({ tipo: 'success', texto: 'Característica agregada.' });
        setCaracNombre('');
        setCaracPadreId('');
    };

    const ruta = (data.ruta || []).map(n => (
        <span key={n.id}>
            <span className="badge"><a href={`#/admin/panel?actualId=${n.id}`}>{n.nombre}</a></span>
            <span className="ruta-separador">/</span>
        </span>
    ));

    return (
        <div className="panel-main">
            <div className="panel-header">
                <h2>Panel de Administrador</h2>
                <span>Bienvenido, <strong>{data.nombre}</strong></span>
            </div>
            <Alert tipo={msg?.tipo} onClose={() => setMsg(null)}>{msg?.texto}</Alert>

            {/* ── GRÁFICAS ── */}
            <GraficasAdmin puestos={data.puestos || []} />

            {/* Empresas pendientes */}
            <div className="section-card">
                <h3>Empresas Pendientes de Aprobacion</h3>
                <table className="panel-table">
                    <thead><tr><th>Nombre</th><th>Correo</th><th>Telefono</th><th>Localizacion</th><th>Accion</th></tr></thead>
                    <tbody>
                        {(data.empresasPendientes || []).length === 0
                            ? <tr><td colSpan="5" className="empty-msg">No hay empresas pendientes.</td></tr>
                            : data.empresasPendientes.map(e => (
                                <tr key={e.id}>
                                    <td>{e.nombre}</td><td>{e.correo ?? '-'}</td>
                                    <td>{e.telefono ?? '-'}</td><td>{e.localizacion ?? '-'}</td>
                                    <td><button className="btn-aprobar" onClick={() => aprobar('empresa', e.id)}>Aprobar</button></td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            </div>

            {/* Oferentes pendientes */}
            <div className="section-card">
                <h3>Oferentes Pendientes de Aprobacion</h3>
                <table className="panel-table">
                    <thead><tr><th>Identificacion</th><th>Nombre</th><th>Correo</th><th>Telefono</th><th>Residencia</th><th>Accion</th></tr></thead>
                    <tbody>
                        {(data.oferentesPendientes || []).length === 0
                            ? <tr><td colSpan="6" className="empty-msg">No hay oferentes pendientes.</td></tr>
                            : data.oferentesPendientes.map(o => (
                                <tr key={o.identificacion}>
                                    <td>{o.identificacion}</td>
                                    <td>{o.nombre} {o.apellido}</td>
                                    <td>{o.correo}</td><td>{o.telefono ?? '-'}</td><td>{o.residencia ?? '-'}</td>
                                    <td><button className="btn-aprobar" onClick={() => aprobar('oferente', o.identificacion)}>Aprobar</button></td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            </div>

            {/* Características */}
            <div className="section-card">
                <h3>Gestion de Caracteristicas</h3>
                <div className="ruta-nav">
                    <span className="badge"><a href="#/admin/panel">Todas las Características</a></span>
                    <span className="ruta-separador">/</span>
                    {ruta}
                    {data.actual && <span className="badge-actual">{data.actual.nombre}</span>}
                </div>
                <p className="subcategoria-label">
                    {data.actual
                        ? <>Subcategorías de: <strong>{data.actual.nombre}</strong></>
                        : 'Mostrando todas las características del sistema'}
                </p>
                <table className="panel-table tabla-margin">
                    <thead><tr><th>Nombre</th><th>Categoria padre</th><th>Accion</th></tr></thead>
                    <tbody>
                        {(data.caracteristicas || []).length === 0
                            ? <tr><td colSpan="3" className="empty-msg">No hay características en este nivel.</td></tr>
                            : data.caracteristicas.map(c => (
                                <tr key={c.id}>
                                    <td>
                                        {data.actual
                                            ? <a className="caracteristica-link" href={`#/admin/panel?actualId=${c.id}`}>{c.nombre}</a>
                                            : c.nombre}
                                    </td>
                                    <td>{c.parent ? <span className="badge">{c.parent.nombre}</span> : <span className="ruta-separador">--</span>}</td>
                                    <td><button className="btn-eliminar" onClick={() => eliminarCaract(c.id)}>Eliminar</button></td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
                <div className="form-divider">
                    <form className="auth-form" onSubmit={agregarCaract}>
                        <div className="form-row">
                            <div className="form-group">
                                <label>Nombre de la caracteristica</label>
                                <input type="text" value={caracNombre} onChange={e => setCaracNombre(e.target.value)}
                                       placeholder="Ej: Java, SQL, Ingles..." required />
                            </div>
                            <div className="form-group">
                                <label>Categoria padre (opcional)</label>
                                <select value={caracPadreId} onChange={e => setCaracPadreId(e.target.value)}>
                                    <option value="">(sin padre)</option>
                                    {(data.todasCaracteristicas || []).map(c => (
                                        <option key={c.id} value={c.id}>{c.nombre}</option>
                                    ))}
                                </select>
                            </div>
                            <button type="submit" className="btn-aprobar">+ Agregar</button>
                        </div>
                    </form>
                </div>
            </div>

            {/* Puestos */}
            <div className="section-card">
                <h3>Puestos Publicados</h3>
                <table className="panel-table">
                    <thead><tr><th>ID</th><th>Empresa</th><th>Descripcion</th><th>Salario</th><th>Tipo</th><th>Estado</th><th>Fecha</th></tr></thead>
                    <tbody>
                        {(data.puestos || []).length === 0
                            ? <tr><td colSpan="7" className="empty-msg">No hay puestos registrados.</td></tr>
                            : data.puestos.map(p => (
                                <tr key={p.id}>
                                    <td>{p.id}</td>
                                    <td>{p.empresa?.nombre ?? '-'}</td>
                                    <td>{(p.descripcion || '').substring(0, 60)}</td>
                                    <td>{fmtSalario(p.moneda, p.salario)}</td>
                                    <td><span className="badge" style={{ background: p.publica ? '#eaf4fb' : '#fdf2fb', color: p.publica ? '#2980b9' : '#8e44ad' }}>{p.publica ? 'Pública' : 'Privada'}</span></td>
                                    <td><span className="badge" style={{ background: p.activo ? '#eafaf1' : '#fdecea', color: p.activo ? '#1e8449' : '#c0392b' }}>{p.activo ? 'Activo' : 'Inactivo'}</span></td>
                                    <td>{fmtFecha(p.fechaPublicacion)}</td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            </div>

            {/* Reporte PDF */}
            <div className="section-card">
                <h3>Reporte de Puestos por Mes</h3>
                <form className="auth-form" style={{ flexDirection: 'row', gap: 12, alignItems: 'flex-end' }}
                      onSubmit={e => {
                          e.preventDefault();
                          const mes = e.target.mes.value;
                          const anio = e.target.anio.value;
                          window.open(`/api/admin/reporte/puestos?mes=${mes}&anio=${anio}`, '_blank');
                      }}>
                    <div className="form-group">
                        <label>Mes</label>
                        <input type="number" name="mes" min="1" max="12"
                               defaultValue={new Date().getMonth() + 1} style={{ width: 80 }} />
                    </div>
                    <div className="form-group">
                        <label>Año</label>
                        <input type="number" name="anio" min="2020" max="2100"
                               defaultValue={new Date().getFullYear()} style={{ width: 100 }} />
                    </div>
                    <button type="submit" className="btn-aprobar">Descargar PDF</button>
                </form>
            </div>
        </div>
    );
}

