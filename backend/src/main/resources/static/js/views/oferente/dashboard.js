// Vista: Dashboard Oferente
class OferenteDashboardView {
    dom;
    state;

    constructor() {
        this.state = {};
        this.dom = this.render();
        this.load();
    }

    render = () => {
        const root = document.createElement('div');
        root.id = 'oferenteDashboard';
        root.innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <h2>Cargando...</h2>
            </div>
        </main>`;
        return root;
    }

    load = async () => {
        const { ok } = await apiGet('/api/oferente/dashboard');
        if (!ok) { navigate('/login'); return; }

        this.dom.innerHTML = `
        <main class="auth-main">
            <div class="auth-card auth-card-wide">
                <div class="auth-header">
                    <h2>Mi Panel</h2>
                    <p>Gestione su perfil, habilidades y CV</p>
                </div>
                <div class="registro-section" style="margin-top:0;">
                    <div class="registro-card">
                        <h3>Mis Habilidades</h3>
                        <p>Agregue o actualice sus habilidades y niveles</p>
                        <a href="#/oferente/habilidades" class="btn btn-secondary">Gestionar habilidades</a>
                    </div>
                    <div class="registro-card">
                        <h3>Suba su currículum en formato PDF para que las empresas lo puedan encontrar</h3>
                        <a href="#/oferente/cv" class="btn btn-primary">Subir CV</a>
                    </div>
                </div>
            </div>
        </main>`;
    }
}

function vistaOferenteDashboard() {
    app().innerHTML = '';
    app().appendChild(new OferenteDashboardView().dom);
}
