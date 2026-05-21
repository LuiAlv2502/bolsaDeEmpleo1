import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiPost } from '../../api';
import Alert from '../../components/Alert';

export default function EmpresaRegistro() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        nombre: '', correo: '', localizacion: '', telefono: '',
        descripcion: '', password: '', confirmarPassword: ''
    });
    const [msg, setMsg] = useState(null); // { tipo: 'error'|'success', texto }
    const [loading, setLoading] = useState(false);

    const set = (campo) => (e) => setForm({ ...form, [campo]: e.target.value });

    const onSubmit = async (e) => {
        e.preventDefault();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(form.correo)) {
            setMsg({ tipo: 'error', texto: 'El correo electrónico no tiene un formato válido.' });
            return;
        }
        setLoading(true);
        const { ok, data } = await apiPost('/api/empresa/registro', form);
        setLoading(false);
        if (!ok) { setMsg({ tipo: 'error', texto: data.error }); return; }
        setMsg({ tipo: 'success', texto: data.mensaje + ' Redirigiendo al login...' });
        setForm({ nombre: '', correo: '', localizacion: '', telefono: '', descripcion: '', password: '', confirmarPassword: '' });
        setTimeout(() => navigate('/login'), 2500);
    };

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Registro de Empresa</h2>
                    <p>Complete los datos para registrar su empresa</p>
                </div>
                <Alert tipo={msg?.tipo} onClose={() => setMsg(null)}>{msg?.texto}</Alert>
                <form className="auth-form" onSubmit={onSubmit}>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Nombre de la empresa</label>
                            <input type="text" value={form.nombre} onChange={set('nombre')}
                                   placeholder="Ej: Tech Solutions S.A." required />
                        </div>
                        <div className="form-group">
                            <label>Localización</label>
                            <input type="text" value={form.localizacion} onChange={set('localizacion')}
                                   placeholder="Ej: San José, Costa Rica" />
                        </div>
                    </div>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Correo electrónico</label>
                            <input type="email" value={form.correo} onChange={set('correo')}
                                   placeholder="empresa@correo.com" required />
                        </div>
                        <div className="form-group">
                            <label>Teléfono</label>
                            <input type="tel" value={form.telefono} onChange={set('telefono')}
                                   placeholder="Ej: +506 8888-8888" />
                        </div>
                    </div>
                    <div className="form-group">
                        <label>Descripción</label>
                        <textarea value={form.descripcion} onChange={set('descripcion')} rows="4"
                                  placeholder="Describe brevemente tu empresa, su misión y áreas de trabajo..." />
                    </div>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Contraseña</label>
                            <input type="password" value={form.password} onChange={set('password')}
                                   placeholder="••••••••" required />
                        </div>
                        <div className="form-group">
                            <label>Confirmar contraseña</label>
                            <input type="password" value={form.confirmarPassword} onChange={set('confirmarPassword')}
                                   placeholder="••••••••" required />
                        </div>
                    </div>
                    <div className="info-box">
                        <p>Su registro será revisado por un administrador antes de poder ingresar al sistema.</p>
                    </div>
                    <button type="submit" className="btn-submit" disabled={loading}>
                        {loading ? 'Registrando...' : 'Registrar Empresa'}
                    </button>
                </form>
                <div className="auth-footer">
                    <p>¿Ya tiene una cuenta? <Link to="/login">Iniciar sesión</Link></p>
                    <p><Link to="/">← Volver al inicio</Link></p>
                </div>
            </div>
        </main>
    );
}
