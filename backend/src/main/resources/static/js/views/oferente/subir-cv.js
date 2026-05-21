// Vista: Subir CV
class OferenteCVView {
    dom;
    state;

    constructor() {
        this.state = { tieneCv: false, ofId: state.usuario?.id ?? '' };
        this.dom = this.render();
        this.load();
    }

    render = () => {
        const root = document.createElement('div');
        root.id = 'oferenteCV';
        root.innerHTML = `
        <main class="auth-main">
            <div class="auth-card">
                <h2>Cargando...</h2>
            </div>
        </main>`;
        return root;
    }

    load = async () => {
        const { ok, data } = await apiGet('/api/oferente/cv');
        if (!ok) { navigate('/login'); return; }

        this.state.tieneCv = data.tieneCv;
        this.dom.innerHTML = this.renderContent();
        this.bindEvents();
    }

    renderContent = () => {
        const { tieneCv, ofId } = this.state;
        return `
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
                         onclick="this.closest('#oferenteCV').querySelector('#archivo').click()">
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
    }

    bindEvents = () => {
        this.dom.querySelector('#archivo').addEventListener('change', (e) => {
            this.dom.querySelector('#nombreArchivo').textContent = e.target.files[0]?.name ?? '';
        });
        this.dom.querySelector('#cvForm').addEventListener('submit', this.subir);
    }

    subir = async (e) => {
        e.preventDefault();
        const archivo = this.dom.querySelector('#archivo').files[0];
        if (!archivo) return;
        const fd = new FormData();
        fd.append('archivo', archivo);
        const res = await fetch('/api/oferente/cv/subir', { method: 'POST', body: fd });
        const d = await res.json().catch(() => ({}));
        const msg = this.dom.querySelector('#msgCV');
        if (!res.ok) { msg.innerHTML = alerta(d.error || 'Error al subir el CV.'); return; }

        msg.innerHTML = alerta(d.mensaje, 'success');
        this.dom.querySelector('.auth-header p').textContent =
            'Su CV está cargado. Puede verlo o reemplazarlo con un nuevo archivo PDF.';
        this.dom.querySelector('#cvForm button[type="submit"]').textContent = 'Reemplazar CV';

        if (!this.dom.querySelector('#verCvLink')) {
            const div = document.createElement('div');
            div.style.marginTop = '18px';
            div.innerHTML = `<a id="verCvLink" href="/api/oferente/cv/ver/${this.state.ofId}"
                target="_blank" class="btn btn-secondary">Ver CV actual</a>`;
            this.dom.querySelector('#cvForm').insertAdjacentElement('afterend', div);
        }
        this.dom.querySelector('#nombreArchivo').textContent = '';
        this.dom.querySelector('#archivo').value = '';
    }
}

function vistaOferenteCV() {
    app().innerHTML = '';
    app().appendChild(new OferenteCVView().dom);
}
