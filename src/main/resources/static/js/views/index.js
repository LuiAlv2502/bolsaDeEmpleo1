// Vista: Inicio (equivalente a templates/index.html)
async function vistaInicio() {
    app().innerHTML = `
    <section class="banner">
        <h2>Bolsa de Empleo</h2>
        <div class="banner-buttons">
            <a href="#/empresa/registro" class="btn btn-primary">Registrar Empresa</a>
            <a href="#/oferente/registro" class="btn btn-secondary">Registrar Oferente</a>
        </div>
    </section>

    <main>
        <section class="search-section">
            <h3>Buscar Puestos de Trabajo</h3>
            <form class="search-form" id="searchForm">
                <div class="form-group">
                    <label for="palabraClave">Palabra Clave</label>
                    <input type="text" id="palabraClave" placeholder="Ej: Desarrollador JavaScript">
                </div>
                <div class="form-group">
                    <label for="caracteristica">Características</label>
                    <select id="caracteristica"><option value="">Todas</option></select>
                </div>
                <div class="form-group">
                    <label for="salarioMin">Salario Mínimo</label>
                    <input type="number" id="salarioMin" placeholder="Ej: 1000">
                </div>
                <button type="submit" class="btn-search">Buscar</button>
                <button type="button" id="btnLimpiar" class="btn-search" style="background:#95a5a6;margin-left:6px;">Limpiar</button>
            </form>
        </section>

        <section class="puestos-section">
            <h3 id="seccionTitulo">Puestos Publicados Recientemente</h3>
            <p id="sinResultados" style="display:none;color:#aaaaaa;font-size:16px;">No se encontraron puestos.</p>
            <div class="puestos-grid" id="puestosGrid"></div>
        </section>
    </main>`;

    const { data } = await apiGet('/api/publico/puestos/buscar');
    const sel = document.getElementById('caracteristica');
    (data.caracteristicas || []).forEach(c => {
        const o = document.createElement('option');
        o.value = c.id; o.textContent = c.nombre; sel.appendChild(o);
    });
    renderPuestosGrid(data.puestosRecientes || []);

    document.getElementById('searchForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const params = new URLSearchParams();
        const p = document.getElementById('palabraClave').value;
        const c = document.getElementById('caracteristica').value;
        const s = document.getElementById('salarioMin').value;
        if (p) params.set('palabra', p);
        if (c) params.set('caracteristica', c);
        if (s) params.set('salarioMin', s);
        const { data: d } = await apiGet('/api/publico/puestos/buscar?' + params);
        const res = d.resultados || [];
        document.getElementById('seccionTitulo').textContent =
            `Resultados de búsqueda (${res.length} encontrados)`;
        renderPuestosGrid(res);
    });

    document.getElementById('btnLimpiar').addEventListener('click', async () => {
        document.getElementById('searchForm').reset();
        const { data: d } = await apiGet('/api/publico/puestos/buscar');
        document.getElementById('seccionTitulo').textContent = 'Puestos Publicados Recientemente';
        renderPuestosGrid(d.puestosRecientes || []);
    });
}

function renderPuestosGrid(puestos) {
    const grid = document.getElementById('puestosGrid');
    const sinRes = document.getElementById('sinResultados');
    grid.innerHTML = '';
    if (!puestos.length) { sinRes.style.display = 'block'; return; }
    sinRes.style.display = 'none';
    puestos.forEach(p => {
        const caract = (p.puestoCaracteristicas || []).length
            ? '<ul>' + p.puestoCaracteristicas.map(pc =>
                `<li>${pc.caracteristica?.nombre ?? ''} — Nivel ${pc.nivelRequerido ?? '-'}</li>`
              ).join('') + '</ul>'
            : '<p style="color:#cccccc;font-size:15px;">Sin requisitos especificados.</p>';
        const card = document.createElement('div');
        card.className = 'puesto-card';
        card.innerHTML = `
            <h4>${p.descripcion ?? 'Sin descripción'}</h4>
            <p class="empresa-nombre">${p.empresa?.nombre ?? '-'}</p>
            <p class="salario">${fmtSalario(p.moneda, p.salario)}</p>
            <p class="fecha">${fmtFecha(p.fechaPublicacion)}</p>
            <div class="tooltip">
                <h5>Características requeridas</h5>
                ${caract}
            </div>`;
        grid.appendChild(card);
    });
}

