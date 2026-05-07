// Vista: Dashboard Empresa (equivalente a templates/empresa/dashboard.html)
async function vistaEmpresaDashboard() {
    const { ok } = await apiGet('/api/empresa/dashboard');
    if (!ok) { navigate('/login'); return; }

    app().innerHTML = `
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

