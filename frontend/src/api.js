/** Devuelve el header Authorization con el JWT almacenado, si existe. */
function authHeader() {
    const token = sessionStorage.getItem('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
}

export async function apiGet(url) {
    const res = await fetch(url, { headers: { ...authHeader() } });
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}

export async function apiPost(url, body = null) {
    const opts = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader() }
    };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(url, opts);
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}

export async function apiDelete(url) {
    const res = await fetch(url, { method: 'DELETE', headers: { ...authHeader() } });
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}

export function fmtSalario(moneda, salario) {
    if (salario == null) return '-';
    const simbolo = moneda === 'CRC' ? '₡' : '$';
    return simbolo + Number(salario).toLocaleString('es-CR', { minimumFractionDigits: 2 });
}

export function fmtFecha(iso) {
    if (!iso) return '-';
    return new Date(iso).toLocaleDateString('es-CR');
}
