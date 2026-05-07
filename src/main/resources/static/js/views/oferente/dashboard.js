// Vista: Dashboard Oferente (equivalente a templates/oferente/dashboard.html)
async function vistaOferenteDashboard() {
    const { ok, data } = await apiGet('/api/oferente/dashboard');
    if (!ok) { navigate('/login'); return; }

    app().innerHTML = `
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

