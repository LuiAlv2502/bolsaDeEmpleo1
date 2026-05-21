// Vista: Login
class LoginView {
    dom;
    state;

    constructor() {
        this.state = {};
        this.dom = this.render();
        this.dom.querySelector('#loginForm').addEventListener('submit', this.onSubmit);
    }

    render = () => {
        const root = document.createElement('div');
        root.id = 'login';
        root.innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <div class="auth-header">
                    <h2>Iniciar Sesión</h2>
                    <p>Ingresa tus credenciales para entrar</p>
                </div>
                <div id="msgLogin"></div>
                <form class="auth-form" id="loginForm">
                    <div class="form-group">
                        <label for="credencial">Correo electrónico o identificación</label>
                        <input type="text" id="credencial"
                               placeholder="tu@correo.com o número de identificación"
                               required autocomplete="username">
                    </div>
                    <div class="form-group">
                        <label for="password">Contraseña</label>
                        <input type="password" id="password"
                               placeholder="••••••••" required autocomplete="current-password">
                    </div>
                    <button type="submit" class="btn-submit">Ingresar</button>
                </form>
                <div class="auth-footer">
                    <p>¿No tiene cuenta?
                        <a href="#/empresa/registro">Registrar empresa</a> ·
                        <a href="#/oferente/registro">Registrarse como Oferente</a>
                    </p>
                    <p><a href="#/">← Volver al inicio</a></p>
                </div>
            </div>
        </main>`;
        return root;
    }

    onSubmit = async (e) => {
        e.preventDefault();
        const credencial = this.dom.querySelector('#credencial').value;
        const password   = this.dom.querySelector('#password').value;
        const { ok, data } = await apiPost('/api/auth/login', { credencial, password });
        const msg = this.dom.querySelector('#msgLogin');
        if (!ok) { msg.innerHTML = alerta(data.error); return; }
        state.usuario = { tipo: data.tipo, nombre: data.nombre, id: data.id };
        renderNav();
        if (data.tipo === 'admin')         navigate('/admin/panel');
        else if (data.tipo === 'empresa')  navigate('/empresa/dashboard');
        else                               navigate('/oferente/dashboard');
    }
}

function vistaLogin() {
    app().innerHTML = '';
    app().appendChild(new LoginView().dom);
}
