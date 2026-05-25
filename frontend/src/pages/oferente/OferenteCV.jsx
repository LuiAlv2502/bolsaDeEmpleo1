import { useEffect, useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiGet } from '../../api';
import { useAuth } from '../../context/AuthContext';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function OferenteCV() {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const [tieneCv, setTieneCv] = useState(false);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [archivo, setArchivo] = useState(null);
    const [msg, setMsg] = useState(null);
    const inputRef = useRef(null);

    useEffect(() => {
        apiGet('/api/oferente/cv').then(({ ok, data }) => {
            if (!ok) { navigate('/login'); return; }
            setTieneCv(data.tieneCv);
            setLoading(false);
        });
    }, [navigate]);

    if (loading) return <Spinner />;

    const subir = async (e) => {
        e.preventDefault();
        if (!archivo) return;
        const fd = new FormData();
        fd.append('archivo', archivo);
        setSubmitting(true);
        const res = await fetch('/api/oferente/cv/subir', { method: 'POST', body: fd });
        const d = await res.json().catch(() => ({}));
        setSubmitting(false);
        if (!res.ok) { setMsg({ tipo: 'error', texto: d.error || 'Error al subir el CV.' }); return; }
        setMsg({ tipo: 'success', texto: d.mensaje });
        setTieneCv(true);
        setArchivo(null);
        if (inputRef.current) inputRef.current.value = '';
    };

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Mi Currículum (CV)</h2>
                    <p>{tieneCv ? 'Su CV está cargado. Puede verlo o reemplazarlo.' : 'Suba su CV en formato PDF.'}</p>
                </div>
                <Alert tipo={msg?.tipo} onClose={() => setMsg(null)}>{msg?.texto}</Alert>
                <form onSubmit={subir}>
                    <div className="cv-drop-area" onClick={() => inputRef.current?.click()}>
                        <span className="cv-icon"></span>
                        <label>Seleccionar archivo PDF</label>
                        <input ref={inputRef} type="file" accept=".pdf" required
                               onChange={e => setArchivo(e.target.files[0] || null)} />
                        <span className="file-hint">Solo archivos .pdf · Máx. 5 MB</span>
                        {archivo && <span className="cv-filename">{archivo.name}</span>}
                    </div>
                    <button type="submit" className="btn-submit" disabled={submitting}>
                        {submitting ? 'Subiendo...' : tieneCv ? 'Reemplazar CV' : 'Subir CV'}
                    </button>
                </form>
                {tieneCv && (
                    <div style={{ marginTop: 18 }}>
                        <a href={`/api/oferente/cv/ver/${usuario?.id}`} target="_blank" rel="noreferrer"
                           className="btn btn-secondary">Ver CV actual</a>
                    </div>
                )}
                <div className="auth-footer">
                    <p><Link to="/oferente/dashboard">← Volver al menú</Link></p>
                </div>
            </div>
        </main>
    );
}
