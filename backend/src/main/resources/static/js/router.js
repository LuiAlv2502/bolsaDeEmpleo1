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
    // Mantener compatibilidad con el resto del código: navigate('/login')
    router.navigate(path);
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

// ─── Router hash (formato objeto) ─────────────────────────────────────────────
// Router sencillo como el que compartiste, con soporte mínimo de rutas dinámicas
// tipo "/empresa/puestos/:id" para no romper el detalle de puesto.

function getHashParts() {
    const raw = window.location.hash.replace('#', '') || '/';
    const [path, queryString = ''] = raw.split('?');
    return {
        path: path || '/',
        queryString
    };
}

function matchRoute(path, routesObj) {
    // 1) match exacto
    if (routesObj[path]) return { action: routesObj[path], params: {} };

    // 2) match dinámico por segmentos ":param"
    const pathSegs = (path || '/').split('/').filter(Boolean);
    for (const key of Object.keys(routesObj)) {
        if (!key.includes(':')) continue;
        const keySegs = key.split('/').filter(Boolean);
        if (keySegs.length !== pathSegs.length) continue;

        const params = {};
        let ok = true;
        for (let i = 0; i < keySegs.length; i++) {
            const ks = keySegs[i];
            const ps = pathSegs[i];
            if (ks.startsWith(':')) {
                params[ks.slice(1)] = ps;
            } else if (ks !== ps) {
                ok = false;
                break;
            }
        }
        if (ok) return { action: routesObj[key], params };
    }

    // 3) fallback
    return { action: routesObj['/'], params: {} };
}

const router = {
    routes: {},

    init(routes) {
        this.routes = routes;

        window.addEventListener('hashchange', () => {
            this.resolve();
        });

        window.addEventListener('load', () => {
            if (!window.location.hash) {
                this.navigate('/');
            } else {
                this.resolve();
            }
        });
    },

    navigate(path) {
        const newHash = `#${path}`;
        if (window.location.hash === newHash) {
            // mismo hash → hashchange no se dispara, forzamos re-render
            this.resolve();
        } else {
            window.location.hash = newHash;
        }
    },

    getCurrentPath() {
        // Importante: NO eliminar querystring aquí.
        return window.location.hash.replace('#', '') || '/';
    },

    async resolve() {
        const { path } = getHashParts();
        window.scrollTo(0, 0);
        renderNav();

        const { action, params } = matchRoute(path, this.routes);
        if (typeof action !== 'function') return;

        try {
            // Si el handler espera parámetros, se los pasamos como objeto.
            // Ej: (p) => vistaEmpresaDetallePuesto(p.id)
            await action(params);
        } catch (err) {
            console.error('Error en vista:', path, err);
            app().innerHTML = `
                <main class="auth-main">
                    <div class="auth-card">
                        <h2>Error inesperado</h2>
                        <p style="color:#c0392b;">${err.message}</p>
                        <p><a href="#/">← Volver al inicio</a></p>
                    </div>
                </main>`;
        }
    }
};

router.init({
    '/': (/*params*/) => vistaInicio(),
    '/login': (/*params*/) => vistaLogin(),

    '/empresa/registro': (/*params*/) => vistaEmpresaRegistro(),
    '/empresa/dashboard': (/*params*/) => vistaEmpresaDashboard(),
    '/empresa/puestos': (/*params*/) => vistaEmpresaPuestos(),
    '/empresa/puestos/:id': (p) => vistaEmpresaDetallePuesto(p.id),

    '/oferente/registro': (/*params*/) => vistaOferenteRegistro(),
    '/oferente/dashboard': (/*params*/) => vistaOferenteDashboard(),
    '/oferente/habilidades': (/*params*/) => vistaOferenteHabilidades(),
    '/oferente/cv': (/*params*/) => vistaOferenteCV(),

    '/admin/panel': (/*params*/) => vistaAdminPanel()
});

