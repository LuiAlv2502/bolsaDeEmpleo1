// ─── Estado global ────────────────────────────────────────────────────────────
const state = {
    get usuario() {
        const s = sessionStorage.getItem('usuario');
        return s ? JSON.parse(s) : null;
    },
    set usuario(val) {
        if (val) sessionStorage.setItem('usuario', JSON.stringify(val));
        else     sessionStorage.removeItem('usuario');
    }
};

// ─── Helpers DOM ──────────────────────────────────────────────────────────────
const app = () => document.getElementById('app');
const nav = () => document.getElementById('mainNav');

// ─── Helpers de formato ───────────────────────────────────────────────────────
function fmtSalario(moneda, salario) {
    if (salario == null) return '-';
    const simbolo = moneda === 'CRC' ? '₡' : '$';
    return simbolo + Number(salario).toLocaleString('es-CR', { minimumFractionDigits: 2 });
}
function fmtFecha(iso) {
    if (!iso) return '-';
    return new Date(iso).toLocaleDateString('es-CR');
}
function alerta(msg, tipo = 'error') {
    return `<div class="alert alert-${tipo}">${msg}</div>`;
}

// ─── Helpers de API ───────────────────────────────────────────────────────────
async function apiGet(url) {
    const res = await fetch(url);
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}
async function apiPost(url, body = null) {
    const opts = { method: 'POST', headers: { 'Content-Type': 'application/json' } };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(url, opts);
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}
async function apiDelete(url) {
    const res = await fetch(url, { method: 'DELETE' });
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}

// ─── Navegación ───────────────────────────────────────────────────────────────
function navigate(path) {
    const newHash = '#' + path;
    if (location.hash === newHash) {
        router(); // mismo hash → hashchange no se dispara, forzamos re-render
    } else {
        location.hash = newHash;
    }
}

function renderNav() {
    const u = state.usuario;
    if (!u) {
        nav().innerHTML = `
            <a href="#/">Inicio</a>
            <a href="#/login">Iniciar Sesión</a>
            <a href="#/empresa/registro">Registrar Empresa</a>
            <a href="#/oferente/registro">Registrar Oferente</a>`;
    } else if (u.tipo === 'empresa') {
        nav().innerHTML = `
            <a href="#/">Inicio</a>
            <a href="#/empresa/dashboard">Dashboard</a>
            <span>Bienvenido, <strong>${u.nombre}</strong></span>
            <a href="#" id="logoutBtn">Cerrar Sesión</a>`;
    } else if (u.tipo === 'oferente') {
        nav().innerHTML = `
            <a href="#/">Inicio</a>
            <a href="#/oferente/dashboard">Dashboard</a>
            <span>Bienvenido, <strong>${u.nombre}</strong></span>
            <a href="#" id="logoutBtn">Cerrar Sesión</a>`;
    } else if (u.tipo === 'admin') {
        nav().innerHTML = `
            <a href="#/">Inicio</a>
            <a href="#/admin/panel">Panel Admin</a>
            <span>Bienvenido, <strong>${u.nombre}</strong></span>
            <a href="#" id="logoutBtn">Cerrar Sesión</a>`;
    }
    document.getElementById('logoutBtn')?.addEventListener('click', async (e) => {
        e.preventDefault();
        await apiPost('/api/auth/logout');
        state.usuario = null;
        renderNav();
        navigate('/');
    });
}

// ─── Router hash ──────────────────────────────────────────────────────────────
const routes = [
    { pattern: /^\/$/, handler: () => vistaInicio() },
    { pattern: /^\/login$/, handler: () => vistaLogin() },
    { pattern: /^\/empresa\/registro$/, handler: () => vistaEmpresaRegistro() },
    { pattern: /^\/empresa\/dashboard$/, handler: () => vistaEmpresaDashboard() },
    { pattern: /^\/empresa\/puestos$/, handler: () => vistaEmpresaPuestos() },
    { pattern: /^\/empresa\/puestos\/(\d+)$/, handler: (m) => vistaEmpresaDetallePuesto(m[1]) },
    { pattern: /^\/oferente\/registro$/, handler: () => vistaOferenteRegistro() },
    { pattern: /^\/oferente\/dashboard$/, handler: () => vistaOferenteDashboard() },
    { pattern: /^\/oferente\/habilidades$/, handler: () => vistaOferenteHabilidades() },
    { pattern: /^\/oferente\/cv$/, handler: () => vistaOferenteCV() },
    { pattern: /^\/admin\/panel/, handler: () => vistaAdminPanel() },
];

async function router() {
    const hash = (location.hash.replace('#', '') || '/').split('?')[0];
    window.scrollTo(0, 0);
    renderNav();
    for (const r of routes) {
        const m = hash.match(r.pattern);
        if (m) {
            try {
                await r.handler(m);
            } catch (err) {
                console.error('Error en vista:', hash, err);
                app().innerHTML = `
                    <main class="auth-main">
                        <div class="auth-card">
                            <h2>Error inesperado</h2>
                            <p style="color:#c0392b;">${err.message}</p>
                            <p><a href="#/">← Volver al inicio</a></p>
                        </div>
                    </main>`;
            }
            return;
        }
    }
    app().innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <h2>404 – Página no encontrada</h2>
                <p><a href="#/">← Volver al inicio</a></p>
            </div>
        </main>`;
}

window.addEventListener('hashchange', router);
window.addEventListener('load', router);

