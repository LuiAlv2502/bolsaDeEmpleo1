export async function apiGet(url) {
    const res = await fetch(url);
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}

export async function apiPost(url, body = null) {
    const opts = { method: 'POST', headers: { 'Content-Type': 'application/json' } };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(url, opts);
    return { ok: res.ok, status: res.status, data: await res.json().catch(() => ({})) };
}

export async function apiDelete(url) {
    const res = await fetch(url, { method: 'DELETE' });
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

