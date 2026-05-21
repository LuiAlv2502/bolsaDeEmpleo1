// Vista: Detalle Puesto
class EmpresaDetallePuestoView {
    dom;
    state;

    constructor(id) {
        this.state = { id, puesto: null, candidatos: [] };
        this.dom = this.render();
        this.load();
    }

    render = () => {
        const root = document.createElement('div');
        root.id = 'empresaDetallePuesto';
        root.innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <h2>Cargando...</h2>
            </div>
        </main>`;
        return root;
    }

    load = async () => {
        const { ok, data } = await apiGet(`/api/empresa/puestos/${this.state.id}/detalle`);
        if (!ok) { navigate('/empresa/puestos'); return; }

        this.state.puesto = data.puesto;
        this.state.candidatos = data.candidatos || [];
        this.dom.innerHTML = this.renderContent();
    }

    renderContent = () => {
        const p = this.state.puesto;
        const tags = (p.puestoCaracteristicas || []).length
            ? `<div class="tags-row">` + p.puestoCaracteristicas.map(pc =>
                `<span class="tag-badge">
                    <span>${pc.caracteristica?.nombre}</span>
                    <span class="nivel-dot">${pc.nivelRequerido}</span>
                 </span>`
              ).join('') + `</div>`
            : '<p style="color:#aaaaaa;font-size:16px;">El puesto no tiene características definidas.</p>';

        const filasCandidatos = this.state.candidatos.map((c, i) => `
            <tr>
                <td>${i + 1}</td>
                <td>${c.oferente.nombre} ${c.oferente.apellido}</td>
                <td>${c.oferente.identificacion}</td>
                <td>${c.oferente.correo}</td>
                <td><span class="match-pct">${c.porcentaje}%</span></td>
                <td>
                    ${c.oferente.cvPdf
                        ? `<a href="/api/oferente/cv/ver/${c.oferente.identificacion}"
                              target="_blank" class="btn-detalle" style="font-size:0.8rem;">Ver CV</a>`
                        : '<span style="color:#aaaaaa;font-size:15px;">Sin CV</span>'}
                </td>
            </tr>`).join('') ||
            `<tr><td colspan="6" class="empty-candidatos">
                No se encontraron oferentes con habilidades coincidentes para este puesto.
             </td></tr>`;

        return `
        <div class="panel-main">
            <div class="panel-header">
                <h2>Detalle del Puesto #${p.id}</h2>
            </div>
            <div class="detalle-card">
                <h3>Información del puesto</h3>
                <div class="detalle-grid">
                    <div class="detalle-item">
                        <label>Salario</label>
                        <span>
                            ${fmtSalario(p.moneda, p.salario)}
                            <small style="font-weight:400;color:#888888;font-size:15px;">${p.moneda}</small>
                        </span>
                    </div>
                    <div class="detalle-item">
                        <label>Tipo de publicación</label>
                        <span style="color:${p.publica ? '#2980b9' : '#8e44ad'};">
                            ${p.publica ? 'Pública' : 'Privada'}
                        </span>
                    </div>
                    <div class="detalle-item">
                        <label>Estado</label>
                        <span style="color:${p.activo ? '#1e8449' : '#c0392b'};">
                            ${p.activo ? 'Activo' : 'Inactivo'}
                        </span>
                    </div>
                    <div class="detalle-item">
                        <label>Fecha de publicación</label>
                        <span>${fmtFecha(p.fechaPublicacion)}</span>
                    </div>
                </div>
                <div class="detalle-descripcion">
                    <label>Descripción</label>
                    <p>${p.descripcion ?? 'Sin descripción.'}</p>
                </div>
                <div style="margin-top:20px;">
                    <label style="font-size:15px;color:#888888;text-transform:uppercase;
                                  letter-spacing:0.04em;display:block;margin-bottom:8px;">
                        Características requeridas
                    </label>
                    ${tags}
                </div>
            </div>
            <div class="detalle-card candidatos-section">
                <h3>Oferentes que calzan con el puesto.</h3>
                <table class="candidatos-table">
                    <thead>
                        <tr>
                            <th>#</th><th>Nombre</th><th>Identificación</th>
                            <th>Correo</th><th>% Coincidencia</th><th>CV</th>
                        </tr>
                    </thead>
                    <tbody>${filasCandidatos}</tbody>
                </table>
            </div>
            <a href="#/empresa/puestos" class="back-link">← Volver a Mis Puestos</a>
        </div>`;
    }
}

function vistaEmpresaDetallePuesto(id) {
    app().innerHTML = '';
    app().appendChild(new EmpresaDetallePuestoView(id).dom);
}
