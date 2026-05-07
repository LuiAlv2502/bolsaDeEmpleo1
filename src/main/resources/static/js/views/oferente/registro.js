// Vista: Registro Oferente (equivalente a templates/oferente/registro.html)
function vistaOferenteRegistro() {
    app().innerHTML = `
    <main class="auth-main">
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <h2>Registro de Oferente</h2>
                <p>Complete sus datos personales para registrarse.</p>
            </div>
            <div id="msgOfReg"></div>
            <form class="auth-form" id="ofRegForm">
                <div class="form-row">
                    <div class="form-group">
                        <label for="identificacion">Identificación</label>
                        <input type="text" id="identificacion" placeholder="Ej: 1-1234-5678" required>
                    </div>
                    <div class="form-group">
                        <label for="nacionalidad">Nacionalidad</label>
                        <input type="text" id="nacionalidad" placeholder="Ej: Costarricense" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="nombre">Nombre</label>
                        <input type="text" id="nombre" placeholder="Tu nombre" required>
                    </div>
                    <div class="form-group">
                        <label for="apellido">Primer apellido</label>
                        <input type="text" id="apellido" placeholder="Tu apellido" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="correo">Correo electrónico</label>
                        <input type="email" id="correo" placeholder="tu@correo.com" required>
                    </div>
                    <div class="form-group">
                        <label for="telefono">Teléfono</label>
                        <input type="tel" id="telefono" placeholder="Ej: +506 8888-8888">
                    </div>
                </div>
                <div class="form-group">
                    <label for="residencia">Lugar de residencia</label>
                    <input type="text" id="residencia" placeholder="Ej: San José, Costa Rica">
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="password">Contraseña</label>
                        <input type="password" id="password" placeholder="••••••••" required>
                    </div>
                    <div class="form-group">
                        <label for="confirmarPassword">Confirmar contraseña</label>
                        <input type="password" id="confirmarPassword" placeholder="••••••••" required>
                    </div>
                </div>
                <div class="info-box">
                    <p>Tu registro será revisado por un administrador antes de poder ingresar al sistema.</p>
                </div>
                <button type="submit" class="btn-submit">Registrarse</button>
            </form>
            <div class="auth-footer">
                <p>¿Ya tienes cuenta? <a href="#/login">Iniciar sesión</a></p>
                <p><a href="#/">← Volver al inicio</a></p>
            </div>
        </div>
    </main>`;

    document.getElementById('ofRegForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const { ok, data } = await apiPost('/api/oferente/registro', {
            identificacion:    document.getElementById('identificacion').value,
            nacionalidad:      document.getElementById('nacionalidad').value,
            nombre:            document.getElementById('nombre').value,
            apellido:          document.getElementById('apellido').value,
            correo:            document.getElementById('correo').value,
            telefono:          document.getElementById('telefono').value,
            residencia:        document.getElementById('residencia').value,
            password:          document.getElementById('password').value,
            confirmarPassword: document.getElementById('confirmarPassword').value
        });
        const msg = document.getElementById('msgOfReg');
        if (!ok) { msg.innerHTML = alerta(data.error); return; }
        msg.innerHTML = alerta(data.mensaje + ' Redirigiendo al login...', 'success');
        document.getElementById('ofRegForm').reset();
        setTimeout(() => navigate('/login'), 2500);
    });
}

