import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiGet } from '../../api';
import { useAuth } from '../../context/AuthContext';

export default function OferenteCV() {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const [tieneCv, setTieneCv] = useState(false);
    const [cargado, setCargado] = useState(false);
    const [archivo, setArchivo] = useState(null);
    const [msg, setMsg] = useState(null);
    const inputRef = useRef(null);

    useEffect(() => {
        apiGet('/api/oferente/cv').then(({ ok, data }) => {
            if (!ok) { navigate('/login'); return; }
            setTieneCv(data.tieneCv);
            setCargado(true);
        });
    }, [navigate]);

    const subir = async (e) => {
        e.preventDefault();
        if (!archivo) return;
        const fd = new FormData();
        fd.append('archivo', archivo);
        const res = await fetch('/api/oferente/cv/subir', { method: 'POST', body: fd });
        const d = await res.json().catch(() => ({}));
        if (!res.ok) { setMsg({ tipo: 'error', texto: d.error || 'Error al subir el CV.' }); return; }
        setMsg({ tipo: 'success', texto: d.mensaje });
        setTieneCv(true);
        setArchivo(null);
        if (inputRef.current) inputRef.current.value = '';
    };

    if (!cargado) return (
        <main className="auth-main">
            <div className="auth-card"><h2>Cargando...</h2></div>
        </main>
    );

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Mi Currículum (CV)</h2>
                    <p>{tieneCv
                        ? 'Su CV está cargado. Puede verlo o reemplazarlo con un nuevo archivo PDF.'
                        : 'Suba su CV en formato PDF para que las empresas lo puedan encontrar.'}</p>
                </div>
                {msg && <div className={`alert alert-${msg.tipo}`}>{msg.texto}</div>}
                <form onSubmit={subir}>
                    <div className="cv-drop-area" onClick={() => inputRef.current?.click()}>
                        <span className="cv-icon">📄</span>
                        <label>Seleccionar archivo PDF</label>
                        <input ref={inputRef} type="file" accept=".pdf" required
                               onChange={e => setArchivo(e.target.files[0] || null)} />
                        <span className="file-hint">Solo archivos .pdf · Máx. 5 MB</span>
                        {archivo && <span className="cv-filename">{archivo.name}</span>}
                    </div>
                    <button type="submit" className="btn-submit">
                        {tieneCv ? 'Reemplazar CV' : 'Subir CV'}
                    </button>
                </form>
                {tieneCv && (
                    <div style={{ marginTop: 18 }}>
                        <a href={`/api/oferente/cv/ver/${usuario?.id}`} target="_blank" rel="noreferrer"
                           className="btn btn-secondary">
                            Ver CV actual
                        </a>
                    </div>
                )}
                <div className="auth-footer">
                    <p><a href="#/oferente/dashboard">← Volver al menú</a></p>
                </div>
            </div>
        </main>
    );
}

