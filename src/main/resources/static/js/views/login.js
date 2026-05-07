// Vista: Login (equivalente a templates/login.html)
function vistaLogin() {
    app().innerHTML = `
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

    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const { ok, data } = await apiPost('/api/auth/login', {
            credencial: document.getElementById('credencial').value,
            password:   document.getElementById('password').value
        });
        if (!ok) {
            document.getElementById('msgLogin').innerHTML = alerta(data.error);
            return;
        }
        state.usuario = { tipo: data.tipo, nombre: data.nombre, id: data.id };
        renderNav();
        if (data.tipo === 'admin')         navigate('/admin/panel');
        else if (data.tipo === 'empresa')  navigate('/empresa/dashboard');
        else                               navigate('/oferente/dashboard');
    });
}

