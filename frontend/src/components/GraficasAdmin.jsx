import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts';

// ── Paletas ──────────────────────────────────────────────────────
const PIE_ESTADO      = ['#2563eb', '#dc2626'];
const PIE_VISIBILIDAD = ['#059669', '#7c3aed'];

// ── Tooltip oscuro personalizado ─────────────────────────────────
const DarkTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length) return null;
    return (
        <div style={{
            background: '#1e293b', color: '#f8fafc',
            padding: '10px 16px', borderRadius: 8, fontSize: 13,
            boxShadow: '0 4px 20px rgba(0,0,0,0.35)'
        }}>
            {label && (
                <p style={{ marginBottom: 6, color: '#94a3b8', fontWeight: 600, fontSize: 12 }}>
                    {label}
                </p>
            )}
            {payload.map((p, i) => (
                <p key={i} style={{ margin: '2px 0' }}>
                    <span style={{ color: p.fill || p.color || '#fff' }}>●</span>{' '}
                    {p.name}: <strong>{p.value}</strong>
                </p>
            ))}
        </div>
    );
};

// ── Etiqueta porcentaje en Pie ───────────────────────────────────
const PieLabel = ({ percent }) =>
    percent > 0 ? `${(percent * 100).toFixed(0)}%` : '';

// ── Estilos reutilizables ────────────────────────────────────────
const card = {
    background: '#fff', borderRadius: 14, padding: '24px 28px',
    marginBottom: 24, boxShadow: '0 4px 14px rgba(0,0,0,0.07)',
    border: '1px solid #e2e8f0'
};
const cardTitle = {
    fontSize: 15, fontWeight: 600, color: '#1e293b',
    marginBottom: 20, paddingBottom: 12, borderBottom: '2px solid #e2e8f0',
};
const axisStyle = { fontSize: 12, fill: '#64748b' };

// ════════════════════════════════════════════════════════════════
export default function GraficasAdmin({ puestos = [] }) {
    if (!puestos.length) return null;

    // ── 1. KPIs ─────────────────────────────────────────────────
    const activos  = puestos.filter(p => p.activo).length;
    const publicos = puestos.filter(p => p.publica).length;
    const empresasUnicas = new Set(puestos.map(p => p.empresa?.nombre)).size;

    const kpis = [
        { label: 'Total puestos', value: puestos.length, bg: '#eff6ff', color: '#2563eb' },
        { label: 'Activos',       value: activos,         bg: '#f0fdf4', color: '#16a34a' },
        { label: 'Inactivos',     value: puestos.length - activos, bg: '#fef2f2', color: '#dc2626' },
        { label: 'Públicos',      value: publicos,         bg: '#faf5ff', color: '#7c3aed' },
        { label: 'Privados',      value: puestos.length - publicos, bg: '#fff7ed', color: '#c2410c' },
        { label: 'Empresas',      value: empresasUnicas,   bg: '#f0fdfa', color: '#0d9488' },
    ];

    // ── 2. Puestos por empresa (top 8) ───────────────────────────
    const porEmpresa = Object.values(
        puestos.reduce((acc, p) => {
            const n = p.empresa?.nombre ?? 'Sin empresa';
            if (!acc[n]) acc[n] = { empresa: n, total: 0, activos: 0 };
            acc[n].total++;
            if (p.activo) acc[n].activos++;
            return acc;
        }, {})
    ).sort((a, b) => b.total - a.total).slice(0, 8);

    // ── 3. Pie: Estado ──────────────────────────────────────────
    const dataPieEstado = [
        { name: 'Activos',   value: activos },
        { name: 'Inactivos', value: puestos.length - activos },
    ].filter(d => d.value > 0);

    // ── 4. Pie: Visibilidad ─────────────────────────────────────
    const dataPieVis = [
        { name: 'Públicos', value: publicos },
        { name: 'Privados', value: puestos.length - publicos },
    ].filter(d => d.value > 0);

    // ── 5. Distribución salarios (CRC) ──────────────────────────
    const rangos = { '< 500k': 0, '500k-1M': 0, '1M-2M': 0, '> 2M': 0 };
    puestos.forEach(p => {
        if (p.salario == null) return;
        const s = Number(p.salario) * (p.moneda === 'USD' ? 530 : 1);
        if      (s < 500_000)   rangos['< 500k']++;
        else if (s < 1_000_000) rangos['500k-1M']++;
        else if (s < 2_000_000) rangos['1M-2M']++;
        else                    rangos['> 2M']++;
    });
    const dataSalario = Object.entries(rangos).map(([rango, cantidad]) => ({ rango, cantidad }));

    // ── 6. Puestos por moneda ────────────────────────────────────
    const crc = puestos.filter(p => p.moneda === 'CRC').length;
    const usd = puestos.filter(p => p.moneda === 'USD').length;
    const dataMoneda = [
        { name: '₡ Colones', value: crc, fill: '#2563eb' },
        { name: '$ Dólares', value: usd, fill: '#059669' },
    ].filter(d => d.value > 0);

    // ────────────────────────────────────────────────────────────
    return (
        <div>
            {/* ── KPIs ─────────────────────────────────────────── */}
            <div style={card}>
                <p style={cardTitle}>Resumen general</p>
                <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
                    {kpis.map(k => (
                        <div key={k.label} style={{
                            background: k.bg, borderRadius: 12,
                            padding: '16px 20px', textAlign: 'center',
                            flex: '1 1 110px', minWidth: 110,
                            border: `1px solid ${k.color}22`
                        }}>
                            <div style={{ fontSize: 28, fontWeight: 700, color: k.color, lineHeight: 1.2 }}>
                                {k.value}
                            </div>
                            <div style={{ fontSize: 12, color: '#64748b', marginTop: 4, fontWeight: 500 }}>
                                {k.label}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* ── Puestos por empresa ──────────────────────────── */}
            <div style={card}>
                <p style={cardTitle}>Puestos publicados por empresa</p>
                <ResponsiveContainer width="100%" height={270}>
                    <BarChart data={porEmpresa} margin={{ top: 4, right: 16, left: 0, bottom: 64 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                        <XAxis dataKey="empresa" tick={axisStyle} angle={-35} textAnchor="end" interval={0} />
                        <YAxis tick={axisStyle} allowDecimals={false} />
                        <Tooltip content={<DarkTooltip />} />
                        <Legend wrapperStyle={{ fontSize: 13, paddingTop: 60 }} iconType="circle" iconSize={10} />
                        <Bar dataKey="total"   name="Total"   fill="#2563eb" radius={[5,5,0,0]} />
                        <Bar dataKey="activos" name="Activos" fill="#059669" radius={[5,5,0,0]} />
                    </BarChart>
                </ResponsiveContainer>
            </div>

            {/* ── Pies: Estado, Visibilidad, Moneda ────────────── */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 24, marginBottom: 24 }}>

                {/* Estado */}
                <div style={{ ...card, marginBottom: 0 }}>
                    <p style={cardTitle}>Estado de puestos</p>
                    <div style={{ display: 'flex', justifyContent: 'center', gap: 24, marginBottom: 16 }}>
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#2563eb' }}>{activos}</div>
                            <div style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>Activos</div>
                        </div>
                        <div style={{ width: 1, background: '#e2e8f0' }} />
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#dc2626' }}>{puestos.length - activos}</div>
                            <div style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>Inactivos</div>
                        </div>
                    </div>
                    <ResponsiveContainer width="100%" height={190}>
                        <PieChart>
                            <Pie data={dataPieEstado} dataKey="value" nameKey="name"
                                cx="50%" cy="50%" innerRadius={50} outerRadius={80}
                                label={PieLabel} labelLine={false} paddingAngle={3}>
                                {dataPieEstado.map((_, i) => <Cell key={i} fill={PIE_ESTADO[i]} stroke="none" />)}
                            </Pie>
                            <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize: 12 }} />
                            <Tooltip content={<DarkTooltip />} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                {/* Visibilidad */}
                <div style={{ ...card, marginBottom: 0 }}>
                    <p style={cardTitle}>Visibilidad</p>
                    <div style={{ display: 'flex', justifyContent: 'center', gap: 24, marginBottom: 16 }}>
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#059669' }}>{publicos}</div>
                            <div style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>Públicos</div>
                        </div>
                        <div style={{ width: 1, background: '#e2e8f0' }} />
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#7c3aed' }}>{puestos.length - publicos}</div>
                            <div style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>Privados</div>
                        </div>
                    </div>
                    <ResponsiveContainer width="100%" height={190}>
                        <PieChart>
                            <Pie data={dataPieVis} dataKey="value" nameKey="name"
                                cx="50%" cy="50%" innerRadius={50} outerRadius={80}
                                label={PieLabel} labelLine={false} paddingAngle={3}>
                                {dataPieVis.map((_, i) => <Cell key={i} fill={PIE_VISIBILIDAD[i]} stroke="none" />)}
                            </Pie>
                            <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize: 12 }} />
                            <Tooltip content={<DarkTooltip />} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                {/* Moneda */}
                <div style={{ ...card, marginBottom: 0 }}>
                    <p style={cardTitle}>Moneda de pago</p>
                    <div style={{ display: 'flex', justifyContent: 'center', gap: 24, marginBottom: 16 }}>
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#2563eb' }}>{crc}</div>
                            <div style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>Colones</div>
                        </div>
                        <div style={{ width: 1, background: '#e2e8f0' }} />
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#059669' }}>{usd}</div>
                            <div style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>Dólares</div>
                        </div>
                    </div>
                    <ResponsiveContainer width="100%" height={190}>
                        <PieChart>
                            <Pie data={dataMoneda} dataKey="value" nameKey="name"
                                cx="50%" cy="50%" innerRadius={50} outerRadius={80}
                                label={PieLabel} labelLine={false} paddingAngle={3}>
                                {dataMoneda.map((d, i) => <Cell key={i} fill={d.fill} stroke="none" />)}
                            </Pie>
                            <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize: 12 }} />
                            <Tooltip content={<DarkTooltip />} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* ── Distribución salarios ────────────────────────── */}
            <div style={card}>
                <p style={cardTitle}>Distribución de salarios (colones)</p>
                <ResponsiveContainer width="100%" height={230}>
                    <BarChart data={dataSalario} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                        <XAxis dataKey="rango" tick={axisStyle} />
                        <YAxis tick={axisStyle} allowDecimals={false} />
                        <Tooltip content={<DarkTooltip />} />
                        <Bar dataKey="cantidad" name="Puestos" radius={[6,6,0,0]}>
                            {dataSalario.map((_, i) => (
                                <Cell key={i} fill={['#2563eb','#059669','#7c3aed','#c2410c'][i]} />
                            ))}
                        </Bar>
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

