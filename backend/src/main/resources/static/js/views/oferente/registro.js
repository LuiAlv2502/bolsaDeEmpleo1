// Vista: Registro Oferente
class OferenteRegistroView {
    dom;
    state;

    constructor() {
        this.state = {};
        this.dom = this.render();
        this.dom.querySelector('#ofRegForm').addEventListener('submit', this.onSubmit);
    }

    render = () => {
        const root = document.createElement('div');
        root.id = 'oferenteRegistro';
        root.innerHTML = `
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
        return root;
    }

    onSubmit = async (e) => {
        e.preventDefault();
        const correo = this.dom.querySelector('#correo').value.trim();
        const msg = this.dom.querySelector('#msgOfReg');

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(correo)) {
            msg.innerHTML = alerta('El correo electrónico no tiene un formato válido.');
            return;
        }

        const { ok, data } = await apiPost('/api/oferente/registro', {
            identificacion:    this.dom.querySelector('#identificacion').value,
            nacionalidad:      this.dom.querySelector('#nacionalidad').value,
            nombre:            this.dom.querySelector('#nombre').value,
            apellido:          this.dom.querySelector('#apellido').value,
            correo,
            telefono:          this.dom.querySelector('#telefono').value,
            residencia:        this.dom.querySelector('#residencia').value,
            password:          this.dom.querySelector('#password').value,
            confirmarPassword: this.dom.querySelector('#confirmarPassword').value
        });
        if (!ok) { msg.innerHTML = alerta(data.error); return; }
        msg.innerHTML = alerta(data.mensaje + ' Redirigiendo al login...', 'success');
        this.dom.querySelector('#ofRegForm').reset();
        setTimeout(() => navigate('/login'), 2500);
    }
}

function vistaOferenteRegistro() {
    app().innerHTML = '';
    app().appendChild(new OferenteRegistroView().dom);
}
