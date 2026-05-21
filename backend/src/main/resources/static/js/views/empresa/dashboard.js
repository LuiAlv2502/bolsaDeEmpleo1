// Vista: Dashboard Empresa
class EmpresaDashboardView {
    dom;
    state;

    constructor() {
        this.state = {};
        this.dom = this.render();
        this.load();
    }

    render = () => {
        const root = document.createElement('div');
        root.id = 'empresaDashboard';
        root.innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <h2>Cargando...</h2>
            </div>
        </main>`;
        return root;
    }

    load = async () => {
        const { ok } = await apiGet('/api/empresa/dashboard');
        if (!ok) { navigate('/login'); return; }

        this.dom.innerHTML = `
        <main class="auth-main">
            <div class="auth-card auth-card-wide">
                <div class="auth-header">
                    <h2>Panel de Empresa</h2>
                    <p>Gestione sus puestos y candidatos</p>
                </div>
                <div class="registro-section" style="margin-top:0;">
                    <div class="registro-card">
                        <h3>Mis Puestos</h3>
                        <p>Publique nuevos puestos y gestione los ya existentes</p>
                        <a href="#/empresa/puestos" class="btn btn-secondary">Ver puestos</a>
                    </div>
                </div>
            </div>
        </main>`;
    }
}

function vistaEmpresaDashboard() {
    app().innerHTML = '';
    app().appendChild(new EmpresaDashboardView().dom);
}
