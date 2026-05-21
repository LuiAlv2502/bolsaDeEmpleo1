import './Spinner.css';

export default function Spinner({ texto = 'Cargando...' }) {
    return (
        <div className="spinner-wrapper">
            <div className="spinner" />
            <p className="spinner-texto">{texto}</p>
        </div>
    );
}

