// Vista: Panel Admin (equivalente a templates/admin/panel.html)
async function vistaAdminPanel() {
    // Mostrar un estado de carga temprano para evitar pantalla en blanco.
    app().innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <h2>Cargando panel de administrador...</h2>
                <p style="color:#7f8c8d;">Espere un momento.</p>
            </div>
        </main>`;

    const hash = location.hash;
    const qs = hash.includes('?') ? hash.split('?')[1] : '';
    const params = new URLSearchParams(qs);
    const actualId = params.get('actualId') || '';
    const url = '/api/admin/panel' + (actualId ? `?actualId=${encodeURIComponent(actualId)}` : '');

    let resp;
    try {
        resp = await apiGet(url);
    } catch (e) {
        console.error('Fallo de red consultando', url, e);
        app().innerHTML = `
            <main class="auth-main">
                <div class="auth-card">
                    <h2>No se pudo cargar el panel</h2>
                    <p style="color:#c0392b;">Error de red al consultar el servidor.</p>
                    <p><a href="#/">← Volver al inicio</a></p>
                </div>
            </main>`;
        return;
    }

    const { ok, status, data } = resp;
    if (!ok) {
        // Si no está autorizado, normalmente es porque no hay sesión admin.
        if (status === 401) {
            app().innerHTML = `
                <main class="auth-main">
                    <div class="auth-card">
                        <h2>No autorizado</h2>
                        <p style="color:#c0392b;">Debe iniciar sesión como administrador para ver este panel.</p>
                        <p><a href="#/login">Ir al login</a></p>
                    </div>
                </main>`;
            return;
        }
        app().innerHTML = `
            <main class="auth-main">
                <div class="auth-card">
                    <h2>No se pudo cargar el panel</h2>
                    <p style="color:#c0392b;">Error ${status}.</p>
                    <p>${(data && data.error) ? data.error : ''}</p>
                    <p><a href="#/">← Volver al inicio</a></p>
                </div>
            </main>`;
        return;
    }

    // ── Empresas pendientes ──────────────────────────────────────────────────
    const filasEmp = (data.empresasPendientes || []).map(e => `
        <tr>
            <td>${e.nombre}</td>
            <td>${e.correo ?? '-'}</td>
            <td>${e.telefono ?? '-'}</td>
            <td>${e.localizacion ?? '-'}</td>
            <td><button class="btn-aprobar" data-tipo="empresa" data-id="${e.id}">Aprobar</button></td>
        </tr>`).join('') ||
        '<tr><td colspan="5" class="empty-msg">No hay empresas pendientes.</td></tr>';

    // ── Oferentes pendientes ─────────────────────────────────────────────────
    const filasOf = (data.oferentesPendientes || []).map(o => `
        <tr>
            <td>${o.identificacion}</td>
            <td>${o.nombre} ${o.apellido}</td>
            <td>${o.correo}</td>
            <td>${o.telefono ?? '-'}</td>
            <td>${o.residencia ?? '-'}</td>
            <td><button class="btn-aprobar" data-tipo="oferente" data-id="${o.identificacion}">Aprobar</button></td>
        </tr>`).join('') ||
        '<tr><td colspan="6" class="empty-msg">No hay oferentes pendientes.</td></tr>';

    // ── Ruta de navegación características ──────────────────────────────────
    const ruta = (data.ruta || []).map(n =>
        `<span class="badge"><a href="#/admin/panel?actualId=${n.id}">${n.nombre}</a></span>
         <span class="ruta-separador">/</span>`
    ).join('');

    // ── Tabla características ────────────────────────────────────────────────
    const filasCaract = (data.caracteristicas || []).map(c => `
        <tr>
            <td>
                ${data.actual
                    ? `<a class="caracteristica-link" href="#/admin/panel?actualId=${c.id}">${c.nombre}</a>`
                    : c.nombre}
            </td>
            <td>
                ${c.parent
                    ? `<span class="badge">${c.parent.nombre}</span>`
                    : '<span class="ruta-separador">--</span>'}
            </td>
            <td>
                <button class="btn-eliminar" data-caract-id="${c.id}">Eliminar</button>
            </td>
        </tr>`).join('') ||
        '<tr><td colspan="3" class="empty-msg">No hay características en este nivel.</td></tr>';

    const optsCaract = (data.todasCaracteristicas || []).map(c =>
        `<option value="${c.id}" ${data.actual?.id === c.id ? 'selected' : ''}>${c.nombre}</option>`
    ).join('');

    // ── Puestos ──────────────────────────────────────────────────────────────
    const filasPuestos = (data.puestos || []).map(p => `
        <tr>
            <td>${p.id}</td>
            <td>${p.empresa?.nombre ?? '-'}</td>
            <td>${(p.descripcion || '').substring(0, 60)}</td>
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
            <td>${fmtFecha(p.fechaPublicacion)}</td>
        </tr>`).join('') ||
        '<tr><td colspan="7" class="empty-msg">No hay puestos registrados.</td></tr>';

    app().innerHTML = `
    <div class="panel-main">
        <div class="panel-header">
            <h2>Panel de Administrador</h2>
            <span>Bienvenido, <strong>${data.nombre}</strong></span>
        </div>
        <div id="msgAdmin"></div>

        <!-- Empresas pendientes -->
        <div class="section-card">
            <h3>Empresas Pendientes de Aprobacion</h3>
            <table class="panel-table">
                <thead>
                    <tr><th>Nombre</th><th>Correo</th><th>Telefono</th><th>Localizacion</th><th>Accion</th></tr>
                </thead>
                <tbody>${filasEmp}</tbody>
            </table>
        </div>

        <!-- Oferentes pendientes -->
        <div class="section-card">
            <h3>Oferentes Pendientes de Aprobacion</h3>
            <table class="panel-table">
                <thead>
                    <tr><th>Identificacion</th><th>Nombre</th><th>Correo</th><th>Telefono</th><th>Residencia</th><th>Accion</th></tr>
                </thead>
                <tbody>${filasOf}</tbody>
            </table>
        </div>

        <!-- Características -->
        <div class="section-card">
            <h3>Gestion de Caracteristicas</h3>

            <div class="ruta-nav">
                <span class="badge"><a href="#/admin/panel">Todas las Características</a></span>
                <span class="ruta-separador">/</span>
                ${ruta}
                ${data.actual ? `<span class="badge-actual">${data.actual.nombre}</span>` : ''}
            </div>

            <p class="subcategoria-label">
                ${data.actual
                    ? `Subcategorías de: <strong>${data.actual.nombre}</strong>`
                    : 'Mostrando todas las características del sistema'}
            </p>

            <table class="panel-table tabla-margin">
                <thead><tr><th>Nombre</th><th>Categoria padre</th><th>Accion</th></tr></thead>
                <tbody>${filasCaract}</tbody>
            </table>

            <div class="form-divider">
                <form class="auth-form" id="caracForm">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="caracNombre">Nombre de la caracteristica</label>
                            <input type="text" id="caracNombre"
                                   placeholder="Ej: Java, SQL, Ingles..." required>
                        </div>
                        <div class="form-group">
                            <label for="caracPadreId">Categoria padre (opcional)</label>
                            <select id="caracPadreId">
                                <option value="">(sin padre)</option>
                                ${optsCaract}
                            </select>
                        </div>
                        <button type="submit" class="btn-aprobar">+ Agregar</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Puestos publicados -->
        <div class="section-card">
            <h3>Puestos Publicados</h3>
            <table class="panel-table">
                <thead>
                    <tr><th>ID</th><th>Empresa</th><th>Descripcion</th><th>Salario</th><th>Tipo</th><th>Estado</th><th>Fecha</th></tr>
                </thead>
                <tbody>${filasPuestos}</tbody>
            </table>
        </div>

        <!-- Reporte PDF -->
        <div class="section-card">
            <h3>Reporte de Puestos por Mes</h3>
            <form class="auth-form" id="reporteForm" style="flex-direction:row;gap:12px;align-items:flex-end;">
                <div class="form-group">
                    <label for="mes">Mes</label>
                    <input type="number" id="mes" min="1" max="12"
                           value="${new Date().getMonth() + 1}" style="width:80px;">
                </div>
                <div class="form-group">
                    <label for="anio">Año</label>
                    <input type="number" id="anio" min="2020" max="2100"
                           value="${new Date().getFullYear()}" style="width:100px;">
                </div>
                <button type="submit" class="btn-aprobar">Descargar PDF</button>
            </form>
        </div>
    </div>`;

    // Aprobar empresa / oferente
    document.querySelectorAll('.btn-aprobar[data-tipo]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const { tipo, id } = btn.dataset;
            const url = tipo === 'empresa'
                ? `/api/admin/empresa/aprobar/${id}`
                : `/api/admin/oferente/aprobar/${id}`;
            const { ok: o, data: d } = await apiPost(url);
            if (o) {
                btn.closest('tr').remove(); // quitar la fila sin re-renderizar
            } else {
                document.getElementById('msgAdmin').innerHTML = alerta(d.error);
            }
        });
    });

    // Eliminar característica
    document.querySelectorAll('.btn-eliminar[data-caract-id]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const { ok: o, data: d } = await apiDelete(`/api/admin/caracteristica/${btn.dataset.caractId}`);
            if (o) {
                btn.closest('tr').remove(); // quitar la fila sin re-renderizar
            } else {
                document.getElementById('msgAdmin').innerHTML = alerta(d.error);
            }
        });
    });

    // Nueva característica
    document.getElementById('caracForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const nombre  = document.getElementById('caracNombre').value;
        const padreId = document.getElementById('caracPadreId').value || null;
        const { ok: o, data: d } = await apiPost('/api/admin/caracteristica/nueva', { nombre, padreId });
        if (!o) { document.getElementById('msgAdmin').innerHTML = alerta(d.error); return; }

        // Insertar nueva fila en la tabla sin re-renderizar
        const tbody = document.querySelector('.panel-table.tabla-margin tbody');
        const padre = (data.todasCaracteristicas || []).find(c => c.id == padreId);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${data.actual
                ? `<a class="caracteristica-link" href="#/admin/panel?actualId=${d.id}">${d.nombre}</a>`
                : d.nombre}</td>
            <td>${padre ? `<span class="badge">${padre.nombre}</span>` : '<span class="ruta-separador">--</span>'}</td>
            <td><button class="btn-eliminar" data-caract-id="${d.id}">Eliminar</button></td>`;
        tbody.appendChild(tr);

        // Registrar evento en la nueva fila
        tr.querySelector('.btn-eliminar').addEventListener('click', async () => {
            const { ok: oe, data: de } = await apiDelete(`/api/admin/caracteristica/${d.id}`);
            if (oe) tr.remove();
            else document.getElementById('msgAdmin').innerHTML = alerta(de.error);
        });

        // Agregar al select de padre también
        const sel = document.getElementById('caracPadreId');
        const opt = document.createElement('option');
        opt.value = d.id; opt.textContent = d.nombre; sel.appendChild(opt);

        document.getElementById('caracNombre').value = '';
        document.getElementById('msgAdmin').innerHTML = alerta('Característica agregada.', 'success');
    });

    // Reporte PDF
    document.getElementById('reporteForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const mes  = document.getElementById('mes').value;
        const anio = document.getElementById('anio').value;
        window.open(`/api/admin/reporte/puestos?mes=${mes}&anio=${anio}`, '_blank');
    });
}

