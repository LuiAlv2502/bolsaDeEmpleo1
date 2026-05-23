import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiPost } from '../../api';

export default function OferenteRegistro() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        identificacion: '', nacionalidad: '', nombre: '', apellido: '',
        correo: '', telefono: '', residencia: '', password: '', confirmarPassword: ''
    });
    const [msg, setMsg] = useState(null);

    const set = (campo) => (e) => setForm({ ...form, [campo]: e.target.value });

    const onSubmit = async (e) => {
        e.preventDefault();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(form.correo)) {
            setMsg({ tipo: 'error', texto: 'El correo electrónico no tiene un formato válido.' });
            return;
        }
        const { ok, data } = await apiPost('/api/oferente/registro', form);
        if (!ok) { setMsg({ tipo: 'error', texto: data.error }); return; }
        setMsg({ tipo: 'success', texto: data.mensaje + ' Redirigiendo al login...' });
        setForm({ identificacion: '', nacionalidad: '', nombre: '', apellido: '', correo: '', telefono: '', residencia: '', password: '', confirmarPassword: '' });
        setTimeout(() => navigate('/login'), 2500);
    };

    return (
        <main className="auth-main">
            <div className="auth-card auth-card-wide">
                <div className="auth-header">
                    <h2>Registro de Oferente</h2>
                    <p>Complete sus datos personales para registrarse.</p>
                </div>
                {msg && <div className={`alert alert-${msg.tipo}`}>{msg.texto}</div>}
                <form className="auth-form" onSubmit={onSubmit}>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Identificación</label>
                            <input type="text" value={form.identificacion} onChange={set('identificacion')}
                                   placeholder="Ej: 1-1234-5678" required />
                        </div>
                        <div className="form-group">
                            <label>Nacionalidad</label>
                            <input type="text" value={form.nacionalidad} onChange={set('nacionalidad')}
                                   placeholder="Ej: Costarricense" required />
                        </div>
                    </div>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Nombre</label>
                            <input type="text" value={form.nombre} onChange={set('nombre')}
                                   placeholder="Tu nombre" required />
                        </div>
                        <div className="form-group">
                            <label>Primer apellido</label>
                            <input type="text" value={form.apellido} onChange={set('apellido')}
                                   placeholder="Tu apellido" required />
                        </div>
                    </div>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Correo electrónico</label>
                            <input type="email" value={form.correo} onChange={set('correo')}
                                   placeholder="tu@correo.com" required />
                        </div>
                        <div className="form-group">
                            <label>Teléfono</label>
                            <input type="tel" value={form.telefono} onChange={set('telefono')}
                                   placeholder="Ej: +506 8888-8888" />
                        </div>
                    </div>
                    <div className="form-group">
                        <label>Lugar de residencia</label>
                        <input type="text" value={form.residencia} onChange={set('residencia')}
                               placeholder="Ej: San José, Costa Rica" />
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
                        <p>Tu registro será revisado por un administrador antes de poder ingresar al sistema.</p>
                    </div>
                    <button type="submit" className="btn-submit">Registrarse</button>
                </form>
                <div className="auth-footer">
                    <p>¿Ya tienes cuenta? <a href="#/login">Iniciar sesión</a></p>
                    <p><a href="#/">← Volver al inicio</a></p>
                </div>
            </div>
        </main>
    );
}

