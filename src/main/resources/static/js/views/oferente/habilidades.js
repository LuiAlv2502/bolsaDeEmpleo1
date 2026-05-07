// Vista: Habilidades Oferente (equivalente a templates/oferente/habilidades.html)
async function vistaOferenteHabilidades() {
    const { ok, data } = await apiGet('/api/oferente/habilidades');
    if (!ok) { navigate('/login'); return; }

    const opts = (data.caracteristicas || []).map(c =>
        `<option value="${c.id}">
            ${c.parent ? c.parent.nombre + ' > ' + c.nombre : c.nombre}
         </option>`
    ).join('');

    const filas = (data.habilidades || []).map(h => `
        <tr data-caract-id="${h.caracteristica?.id ?? ''}">
            <td>${h.caracteristica?.nombre ?? '-'}</td>
            <td>
                ${h.caracteristica?.parent
                    ? `<span class="badge">${h.caracteristica.parent.nombre}</span>`
                    : '<span style="color:#aaaaaa;">-</span>'}
            </td>
            <td><span class="nivel-badge">${h.nivel}</span></td>
            <td>
                <button class="btn-eliminar" data-id="${h.id}">Eliminar</button>
            </td>
        </tr>`).join('');

    app().innerHTML = `
    <main class="auth-main">
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <h2>Mis Habilidades</h2>
                <p>Agregue o actualice sus habilidades y niveles de dominio.</p>
            </div>
            <div id="msgHab"></div>

            <div class="section-card" style="margin-bottom:24px;">
                <h3>Agregar / actualizar habilidades</h3>
                <form class="auth-form" id="habForm">
                    <div class="form-row">
                        <div class="form-group" style="flex:3;">
                            <label for="caracteristicaId">Característica</label>
                            <select id="caracteristicaId" required>
                                <option value="">Seleccionar</option>
                                ${opts}
                            </select>
                        </div>
                        <div class="form-group" style="flex:1;">
                            <label for="nivel">Nivel (1-5)</label>
                            <input type="number" id="nivel" min="1" max="5" value="1" required>
                        </div>
                        <div class="form-group" style="flex:1;justify-content:flex-end;padding-top:22px;">
                            <button type="submit" class="btn-submit" style="margin:0;padding:10px 18px;">
                                + Agregar
                            </button>
                        </div>
                    </div>
                    <p style="font-size:15px;color:#888888;margin-top:-8px;">
                        Nivel 1 = básico · 3 = intermedio · 5 = experto.
                        Si la habilidad ya existe, se actualizará el nivel de la misma.
                    </p>
                </form>
            </div>

            <div class="section-card">
                <h3>Mis habilidades registradas</h3>
                ${!filas
                    ? '<div class="empty-msg">Aún no tiene habilidades registradas.</div>'
                    : `<table class="panel-table" id="tablaHabilidades">
                        <thead><tr><th>Característica</th><th>Categoría padre</th><th>Nivel</th><th>Acción</th></tr></thead>
                        <tbody>${filas}</tbody>
                       </table>`}
            </div>

            <div class="auth-footer">
                <p><a href="#/oferente/dashboard">← Volver al menú</a></p>
            </div>
        </div>
    </main>`;

    document.getElementById('habForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const caractId = document.getElementById('caracteristicaId').value;
        const nivel    = document.getElementById('nivel').value;
        const { ok: o, data: d } = await apiPost('/api/oferente/habilidades/agregar', {
            caracteristicaId: caractId, nivel
        });
        const msg = document.getElementById('msgHab');
        if (!o) { msg.innerHTML = alerta(d.error); return; }

        // Buscar la característica seleccionada para mostrarla en la tabla
        const sel    = document.getElementById('caracteristicaId');
        const texto  = sel.options[sel.selectedIndex].text;
        const esPadre = texto.includes(' > ');
        const nombre = esPadre ? texto.split(' > ')[1] : texto;
        const padre  = esPadre ? texto.split(' > ')[0] : null;

        // Verificar si ya existe la fila (actualización de nivel)
        let filaExistente = null;
        document.querySelectorAll('#tablaHabilidades tbody tr').forEach(tr => {
            if (tr.dataset.caractId == caractId) filaExistente = tr;
        });

        if (filaExistente) {
            filaExistente.querySelector('.nivel-badge').textContent = nivel;
            msg.innerHTML = alerta('Nivel actualizado.', 'success');
        } else {
            // Insertar nueva fila
            let tbody = document.querySelector('#tablaHabilidades tbody');
            if (!tbody) {
                // La tabla no existía (estaba el empty-msg), crearla
                document.querySelector('.section-card:last-of-type').innerHTML = `
                    <h3>Mis habilidades registradas</h3>
                    <table class="panel-table" id="tablaHabilidades">
                        <thead><tr><th>Característica</th><th>Categoría padre</th><th>Nivel</th><th>Acción</th></tr></thead>
                        <tbody></tbody>
                    </table>`;
                tbody = document.querySelector('#tablaHabilidades tbody');
            }
            const tr = document.createElement('tr');
            tr.dataset.caractId = caractId;
            tr.innerHTML = `
                <td>${nombre}</td>
                <td>${padre ? `<span class="badge">${padre}</span>` : '<span style="color:#aaaaaa;">-</span>'}</td>
                <td><span class="nivel-badge">${nivel}</span></td>
                <td><button class="btn-eliminar" data-id="${d.id ?? ''}">Eliminar</button></td>`;
            tbody.appendChild(tr);
            registrarEliminarHabilidad(tr.querySelector('.btn-eliminar'));
            msg.innerHTML = alerta('Habilidad agregada.', 'success');
        }
        document.getElementById('habForm').reset();
    });

    function registrarEliminarHabilidad(btn) {
        btn.addEventListener('click', async () => {
            await apiDelete(`/api/oferente/habilidades/${btn.dataset.id}`);
            btn.closest('tr').remove();
        });
    }

    document.querySelectorAll('.btn-eliminar').forEach(btn => registrarEliminarHabilidad(btn));
}

