import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { apiGet, fmtSalario, fmtFecha } from '../../api';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function EmpresaDetallePuesto() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [puesto, setPuesto] = useState(null);
    const [candidatos, setCandidatos] = useState([]);

    useEffect(() => {
        apiGet(`/api/empresa/puestos/${id}/detalle`).then(({ ok, data }) => {
            if (!ok) { navigate('/empresa/puestos'); return; }
            setPuesto(data.puesto);
            setCandidatos(data.candidatos || []);
        });
    }, [id, navigate]);

    if (!puesto) return <Spinner />;

    return (
        <div className="panel-main">
            <div className="panel-header">
                <h2>Detalle del Puesto #{puesto.id}</h2>
            </div>

            <div className="detalle-card">
                <h3>Información del puesto</h3>
                <div className="detalle-grid">
                    <div className="detalle-item">
                        <label>Salario</label>
                        <span>{fmtSalario(puesto.moneda, puesto.salario)} <small style={{ fontWeight: 400, color: '#888', fontSize: 15 }}>{puesto.moneda}</small></span>
                    </div>
                    <div className="detalle-item">
                        <label>Tipo de publicación</label>
                        <span style={{ color: puesto.publica ? '#2980b9' : '#8e44ad' }}>
                            {puesto.publica ? 'Pública' : 'Privada'}
                        </span>
                    </div>
                    <div className="detalle-item">
                        <label>Estado</label>
                        <span style={{ color: puesto.activo ? '#1e8449' : '#c0392b' }}>
                            {puesto.activo ? 'Activo' : 'Inactivo'}
                        </span>
                    </div>
                    <div className="detalle-item">
                        <label>Fecha de publicación</label>
                        <span>{fmtFecha(puesto.fechaPublicacion)}</span>
                    </div>
                </div>
                <div className="detalle-descripcion">
                    <label>Descripción</label>
                    <p>{puesto.descripcion ?? 'Sin descripción.'}</p>
                </div>
                <div style={{ marginTop: 20 }}>
                    <label style={{ fontSize: 15, color: '#888', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block', marginBottom: 8 }}>
                        Características requeridas
                    </label>
                    {(puesto.puestoCaracteristicas || []).length
                        ? (
                            <div className="tags-row">
                                {puesto.puestoCaracteristicas.map(pc => (
                                    <span key={pc.caracteristica?.id} className="tag-badge">
                                        {pc.caracteristica?.nombre}
                                        <span className="nivel-dot">{pc.nivelRequerido}</span>
                                    </span>
                                ))}
                            </div>
                        )
                        : <p style={{ color: '#aaaaaa', fontSize: 16 }}>El puesto no tiene características definidas.</p>
                    }
                </div>
            </div>

            <div className="detalle-card candidatos-section">
                <h3>Oferentes que calzan con el puesto.</h3>
                <table className="candidatos-table">
                    <thead>
                        <tr><th>#</th><th>Nombre</th><th>Identificación</th><th>Correo</th><th>% Coincidencia</th><th>CV</th></tr>
                    </thead>
                    <tbody>
                        {candidatos.length === 0
                            ? <tr><td colSpan="6" className="empty-candidatos">No se encontraron oferentes con habilidades coincidentes para este puesto.</td></tr>
                            : candidatos.map((c, i) => (
                                <tr key={c.oferente.identificacion}>
                                    <td>{i + 1}</td>
                                    <td>{c.oferente.nombre} {c.oferente.apellido}</td>
                                    <td>{c.oferente.identificacion}</td>
                                    <td>{c.oferente.correo}</td>
                                    <td><span className="match-pct">{c.porcentaje}%</span></td>
                                    <td>
                                        {c.oferente.cvPdf
                                            ? <a href={`/api/oferente/cv/ver/${c.oferente.identificacion}`} target="_blank" rel="noreferrer" className="btn-detalle" style={{ fontSize: '0.8rem' }}>Ver CV</a>
                                            : <span style={{ color: '#aaaaaa', fontSize: 15 }}>Sin CV</span>
                                        }
                                    </td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            </div>

            <Link to="/empresa/puestos" className="back-link">← Volver a Mis Puestos</Link>
        </div>
    );
}
