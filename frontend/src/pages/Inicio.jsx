import { useState, useEffect } from 'react';
import { apiGet } from '../api';
import { fmtSalario, fmtFecha } from '../api';

export default function Inicio() {
    const [caracteristicas, setCaracteristicas] = useState([]);
    const [puestos, setPuestos] = useState([]);
    const [titulo, setTitulo] = useState('Puestos Publicados Recientemente');
    const [sinResultados, setSinResultados] = useState(false);
    const [palabraClave, setPalabraClave] = useState('');
    const [caracteristicaId, setCaracteristicaId] = useState('');
    const [salarioMin, setSalarioMin] = useState('');

    useEffect(() => {
        apiGet('/api/publico/puestos/buscar').then(({ data }) => {
            setCaracteristicas(data.caracteristicas || []);
            renderGrid(data.puestosRecientes || []);
        });
    }, []);

    const renderGrid = (lista) => {
        setSinResultados(lista.length === 0);
        setPuestos(lista);
    };

    const search = async (e) => {
        e.preventDefault();
        const params = new URLSearchParams();
        if (palabraClave) params.set('palabra', palabraClave);
        if (caracteristicaId) params.set('caracteristica', caracteristicaId);
        if (salarioMin) params.set('salarioMin', salarioMin);
        const { data } = await apiGet('/api/publico/puestos/buscar?' + params);
        const res = data.resultados || [];
        setTitulo(`Resultados de búsqueda (${res.length} encontrados)`);
        renderGrid(res);
    };

    const limpiar = async () => {
        setPalabraClave('');
        setCaracteristicaId('');
        setSalarioMin('');
        const { data } = await apiGet('/api/publico/puestos/buscar');
        setTitulo('Puestos Publicados Recientemente');
        renderGrid(data.puestosRecientes || []);
    };

    return (
        <>
            <section className="banner">
                <h2>Bolsa de Empleo</h2>
                <div className="banner-buttons">
                    <a href="#/empresa/registro" className="btn btn-primary">Registrar Empresa</a>
                    <a href="#/oferente/registro" className="btn btn-secondary">Registrar Oferente</a>
                </div>
            </section>
            <main>
                <section className="search-section">
                    <h3>Buscar Puestos de Trabajo</h3>
                    <form className="search-form" onSubmit={search}>
                        <div className="form-group">
                            <label>Palabra Clave</label>
                            <input type="text" value={palabraClave}
                                   onChange={e => setPalabraClave(e.target.value)}
                                   placeholder="Ej: Desarrollador JavaScript" />
                        </div>
                        <div className="form-group">
                            <label>Características</label>
                            <select value={caracteristicaId} onChange={e => setCaracteristicaId(e.target.value)}>
                                <option value="">Todas</option>
                                {caracteristicas.map(c => (
                                    <option key={c.id} value={c.id}>{c.nombre}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Salario Mínimo</label>
                            <input type="number" value={salarioMin}
                                   onChange={e => setSalarioMin(e.target.value)}
                                   placeholder="Ej: 1000" />
                        </div>
                        <button type="submit" className="btn-search">Buscar</button>
                        <button type="button" className="btn-search"
                                style={{ background: '#95a5a6', marginLeft: 6 }}
                                onClick={limpiar}>Limpiar</button>
                    </form>
                </section>
                <section className="puestos-section">
                    <h3>{titulo}</h3>
                    {sinResultados && (
                        <p style={{ color: '#aaaaaa', fontSize: 16 }}>No se encontraron puestos.</p>
                    )}
                    <div className="puestos-grid">
                        {puestos.map(p => {
                            const caract = (p.puestoCaracteristicas || []).length
                                ? p.puestoCaracteristicas.map(pc =>
                                    <li key={pc.caracteristica?.id}>{pc.caracteristica?.nombre} — Nivel {pc.nivelRequerido ?? '-'}</li>
                                  )
                                : null;
                            return (
                                <div key={p.id} className="puesto-card">
                                    <h4>{p.descripcion ?? 'Sin descripción'}</h4>
                                    <p className="empresa-nombre">{p.empresa?.nombre ?? '-'}</p>
                                    <p className="salario">{fmtSalario(p.moneda, p.salario)}</p>
                                    <p className="fecha">{fmtFecha(p.fechaPublicacion)}</p>
                                    <div className="tooltip">
                                        <h5>Características requeridas</h5>
                                        {caract
                                            ? <ul>{caract}</ul>
                                            : <p style={{ color: '#cccccc', fontSize: 15 }}>Sin requisitos especificados.</p>
                                        }
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </section>
            </main>
        </>
    );
}

