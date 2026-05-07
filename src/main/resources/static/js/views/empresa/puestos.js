// Vista: Puestos Empresa (equivalente a templates/empresa/puestos.html)
async function vistaEmpresaPuestos() {
    const { ok, data } = await apiGet('/api/empresa/puestos');
    if (!ok) { navigate('/login'); return; }

    const selectOpts = (data.caracteristicas || [])
        .map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');

    const filasPuestos = (data.puestos || []).map(p => {
        const tags = (p.puestoCaracteristicas || []).length
            ? p.puestoCaracteristicas.map(pc =>
                `<span class="badge">${pc.caracteristica?.nombre} (Nv.${pc.nivelRequerido})</span>`
              ).join(' ')
            : '—';
        return `
        <tr>
            <td>${p.id}</td>
            <td>${(p.descripcion || '').substring(0, 80)}</td>
            <td>${fmtSalario(p.moneda, p.salario)}</td>
            <td>
                <span class="badge" style="background:${p.publica ? '#eaf4fb' : '#fdf2fb'};
                      color:${p.publica ? '#2980b9' : '#8e44ad'};">
                    ${p.publica ? 'Pública' : 'Privada'}
                </span>
            </td>
            <td>
                <span class="badge" style="background:${p.activo ? '#eafaf1' : '#fdecea'};
                      color:${p.activo ? '#1e8449' : '#c0392b'};">
                    ${p.activo ? 'Activo' : 'Inactivo'}
                </span>
            </td>
            <td>${tags}</td>
            <td>
                <a href="#/empresa/puestos/${p.id}" class="btn-detalle">Ver detalles</a>
                ${p.activo
                    ? `<button class="btn-desactivar" data-id="${p.id}">Desactivar</button>`
                    : '<span style="color:#aaaaaa;font-size:15px;">—</span>'}
            </td>
        </tr>`;
    }).join('');

    const filasRequeridas = [1,2,3,4,5].map(() => `
        <div class="form-row" style="align-items:flex-end;margin-bottom:8px;">
            <div class="form-group" style="flex:2;">
                <select class="caract-sel">
                    <option value="">Ninguna</option>
                    ${selectOpts}
                </select>
            </div>
            <div class="form-group" style="flex:1;">
                <input type="number" class="nivel-inp" min="1" max="5" value="1" placeholder="Nivel (1-5)">
            </div>
        </div>`).join('');

    app().innerHTML = `
    <div class="panel-main">
        <div class="panel-header"><h2>Mis Puestos</h2></div>
        <div id="msgPuestos"></div>

        <div class="section-card">
            <h3>Publicar Nuevo Puesto</h3>
            <form class="auth-form" id="pubForm">
                <div class="form-group">
                    <label for="descripcion">Descripción general del puesto</label>
                    <textarea id="descripcion" rows="1" style="resize:none"
                              placeholder="Inserte el nombre del puesto." required></textarea>
                </div>
                <div class="form-row">
                    <div class="form-group" style="flex:2;">
                        <label for="salario">Salario ofrecido</label>
                        <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
                            <input type="number" id="salario" min="0" step="0.01"
                                   placeholder="Ej: 850000" required style="flex:1;min-width:120px;">
                            <input type="radio" class="moneda-radio" id="moneda-crc" name="moneda" value="CRC" checked>
                            <label for="moneda-crc" class="btn-moneda">₡ Colones</label>
                            <input type="radio" class="moneda-radio" id="moneda-usd" name="moneda" value="USD">
                            <label for="moneda-usd" class="btn-moneda">$ Dólares</label>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="publica">Tipo de publicación</label>
                        <select id="publica">
                            <option value="true">Pública</option>
                            <option value="false">Privada</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label>Características requeridas
                        <small style="font-weight:normal;color:#888888;">(deje en blanco las que no use, máx. 5)</small>
                    </label>
                    ${filasRequeridas}
                </div>
                <button type="submit" class="btn-submit">Publicar Puesto</button>
            </form>
        </div>

        <div class="section-card">
            <h3>Puestos Registrados</h3>
            ${!filasPuestos
                ? '<div class="empty-msg">No tiene puestos publicados aún.</div>'
                : `<table class="panel-table" id="tablaPuestos">
                    <thead>
                        <tr>
                            <th>#</th><th>Descripción</th><th>Salario</th>
                            <th>Tipo</th><th>Estado</th><th>Características</th><th>Acción</th>
                        </tr>
                    </thead>
                    <tbody>${filasPuestos}</tbody>
                   </table>`}
        </div>
    </div>`;

    document.getElementById('pubForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const sels  = [...document.querySelectorAll('.caract-sel')];
        const nivs  = [...document.querySelectorAll('.nivel-inp')];
        const ids = [], niveles = [];
        sels.forEach((s, i) => {
            if (s.value) { ids.push(s.value); niveles.push(nivs[i].value); }
        });
        const { ok: o, data: d } = await apiPost('/api/empresa/publicarPuesto', {
            descripcion: document.getElementById('descripcion').value,
            salario:     document.getElementById('salario').value,
            publica:     document.getElementById('publica').value,
            moneda:      document.querySelector('input[name="moneda"]:checked').value,
            caracteristicaIds: ids,
            niveles
        });
        const msg = document.getElementById('msgPuestos');
        if (!o) { msg.innerHTML = alerta(d.error || 'Error al publicar.'); return; }

        // Insertar nueva fila en la tabla sin recargar
        let tbody = document.querySelector('#tablaPuestos tbody');
        if (!tbody) {
            // La tabla no existía, crearla
            document.querySelector('.section-card:last-of-type').innerHTML = `
                <h3>Puestos Registrados</h3>
                <table class="panel-table" id="tablaPuestos">
                    <thead><tr><th>#</th><th>Descripción</th><th>Salario</th><th>Tipo</th><th>Estado</th><th>Características</th><th>Acción</th></tr></thead>
                    <tbody></tbody>
                </table>`;
            tbody = document.querySelector('#tablaPuestos tbody');
        }
        const tags = (d.puestoCaracteristicas || []).length
            ? d.puestoCaracteristicas.map(pc =>
                `<span class="badge">${pc.caracteristica?.nombre} (Nv.${pc.nivelRequerido})</span>`
              ).join(' ')
            : '—';
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${d.id}</td>
            <td>${(d.descripcion || '').substring(0, 80)}</td>
            <td>${fmtSalario(d.moneda, d.salario)}</td>
            <td><span class="badge" style="background:#eaf4fb;color:#2980b9;">${d.publica ? 'Pública' : 'Privada'}</span></td>
            <td><span class="badge" style="background:#eafaf1;color:#1e8449;">Activo</span></td>
            <td>${tags}</td>
            <td>
                <a href="#/empresa/puestos/${d.id}" class="btn-detalle">Ver detalles</a>
                <button class="btn-desactivar" data-id="${d.id}">Desactivar</button>
            </td>`;
        tbody.appendChild(tr);
        registrarDesactivar(tr.querySelector('.btn-desactivar'));

        msg.innerHTML = alerta('Puesto publicado correctamente.', 'success');
        document.getElementById('pubForm').reset();
    });

    function registrarDesactivar(btn) {
        btn.addEventListener('click', async () => {
            await apiPost(`/api/empresa/puestos/${btn.dataset.id}/desactivar`);
            const td = btn.closest('tr').querySelector('td:nth-child(5)');
            td.innerHTML = '<span class="badge" style="background:#fdecea;color:#c0392b;">Inactivo</span>';
            btn.remove();
        });
    }

    document.querySelectorAll('.btn-desactivar').forEach(btn => registrarDesactivar(btn));
}

