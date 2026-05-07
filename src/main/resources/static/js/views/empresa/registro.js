// Vista: Registro Empresa (equivalente a templates/empresa/registro.html)
function vistaEmpresaRegistro() {
    app().innerHTML = `
    <main class="auth-main">
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <h2>Registro de Empresa</h2>
                <p>Complete los datos para registrar su empresa</p>
            </div>
            <div id="msgEmpReg"></div>
            <form class="auth-form" id="empRegForm">
                <div class="form-row">
                    <div class="form-group">
                        <label for="nombre">Nombre de la empresa</label>
                        <input type="text" id="nombre" placeholder="Ej: Tech Solutions S.A." required>
                    </div>
                    <div class="form-group">
                        <label for="localizacion">Localización</label>
                        <input type="text" id="localizacion" placeholder="Ej: San José, Costa Rica">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="correo">Correo electrónico</label>
                        <input type="email" id="correo" placeholder="empresa@correo.com" required>
                    </div>
                    <div class="form-group">
                        <label for="telefono">Teléfono</label>
                        <input type="tel" id="telefono" placeholder="Ej: +506 8888-8888">
                    </div>
                </div>
                <div class="form-group">
                    <label for="descripcion">Descripción</label>
                    <textarea id="descripcion" rows="4"
                              placeholder="Describe brevemente tu empresa, su misión y áreas de trabajo..."></textarea>
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
                    <p>Su registro será revisado por un administrador antes de poder ingresar al sistema.</p>
                </div>
                <button type="submit" class="btn-submit">Registrar Empresa</button>
            </form>
            <div class="auth-footer">
                <p>¿Ya tiene una cuenta? <a href="#/login">Iniciar sesión</a></p>
                <p><a href="#/">← Volver al inicio</a></p>
            </div>
        </div>
    </main>`;

    document.getElementById('empRegForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const { ok, data } = await apiPost('/api/empresa/registro', {
            nombre:            document.getElementById('nombre').value,
            correo:            document.getElementById('correo').value,
            localizacion:      document.getElementById('localizacion').value,
            telefono:          document.getElementById('telefono').value,
            descripcion:       document.getElementById('descripcion').value,
            password:          document.getElementById('password').value,
            confirmarPassword: document.getElementById('confirmarPassword').value
        });
        const msg = document.getElementById('msgEmpReg');
        if (!ok) { msg.innerHTML = alerta(data.error); return; }
        msg.innerHTML = alerta(data.mensaje + ' Redirigiendo al login...', 'success');
        document.getElementById('empRegForm').reset();
        setTimeout(() => navigate('/login'), 2500);
    });
}

