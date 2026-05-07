// Vista: Subir CV (equivalente a templates/oferente/subir-cv.html)
async function vistaOferenteCV() {
    const { ok, data } = await apiGet('/api/oferente/cv');
    if (!ok) { navigate('/login'); return; }

    const tieneCv = data.tieneCv;
    const ofId    = state.usuario?.id ?? '';

    app().innerHTML = `
    <main class="auth-main">
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <h2>Mi Currículum (CV)</h2>
                <p>${tieneCv
                    ? 'Su CV está cargado. Puede verlo o reemplazarlo con un nuevo archivo PDF.'
                    : 'Suba su CV en formato PDF para que las empresas lo puedan encontrar.'}</p>
            </div>
            <div id="msgCV"></div>

            <form id="cvForm">
                <div class="cv-drop-area" id="dropArea"
                     onclick="document.getElementById('archivo').click()">
                    <span class="cv-icon">📄</span>
                    <label>Seleccionar archivo PDF</label>
                    <input type="file" id="archivo" name="archivo" accept=".pdf" required>
                    <span class="file-hint">Solo archivos .pdf · Máx. 5 MB</span>
                    <span class="cv-filename" id="nombreArchivo"></span>
                </div>
                <button type="submit" class="btn-submit">
                    ${tieneCv ? 'Reemplazar CV' : 'Subir CV'}
                </button>
            </form>

            ${tieneCv
                ? `<div style="margin-top:18px;">
                       <a href="/api/oferente/cv/ver/${ofId}" target="_blank" class="btn btn-secondary">
                           Ver CV actual
                       </a>
                   </div>`
                : ''}

            <div class="auth-footer">
                <p><a href="#/oferente/dashboard">← Volver al menú</a></p>
            </div>
        </div>
    </main>`;

    document.getElementById('archivo').addEventListener('change', (e) => {
        document.getElementById('nombreArchivo').textContent = e.target.files[0]?.name ?? '';
    });

    document.getElementById('cvForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const archivo = document.getElementById('archivo').files[0];
        if (!archivo) return;
        const fd = new FormData();
        fd.append('archivo', archivo);
        const res = await fetch('/api/oferente/cv/subir', { method: 'POST', body: fd });
        const d = await res.json().catch(() => ({}));
        const msg = document.getElementById('msgCV');
        if (!res.ok) { msg.innerHTML = alerta(d.error || 'Error al subir el CV.'); return; }

        // Actualizar solo el texto y el botón sin recargar
        msg.innerHTML = alerta(d.mensaje, 'success');
        document.querySelector('.auth-header p').textContent =
            'Su CV está cargado. Puede verlo o reemplazarlo con un nuevo archivo PDF.';
        document.querySelector('#cvForm button[type="submit"]').textContent = 'Reemplazar CV';

        // Mostrar botón "Ver CV" si no existía
        if (!document.getElementById('verCvLink')) {
            const div = document.createElement('div');
            div.style.marginTop = '18px';
            div.innerHTML = `<a id="verCvLink" href="/api/oferente/cv/ver/${ofId}"
                target="_blank" class="btn btn-secondary">Ver CV actual</a>`;
            document.getElementById('cvForm').insertAdjacentElement('afterend', div);
        }
        document.getElementById('nombreArchivo').textContent = '';
        document.getElementById('archivo').value = '';
    });
}

