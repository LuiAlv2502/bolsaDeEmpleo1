/**
 * Alert reutilizable.
 * tipo: 'error' | 'success' | 'info' | 'warning'
 */
export default function Alert({ tipo = 'error', children, onClose }) {
    if (!children) return null;
    return (
        <div className={`alert alert-${tipo}`} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <span>{children}</span>
            {onClose && (
                <button onClick={onClose}
                    style={{ background: 'none', border: 'none', cursor: 'pointer',
                             fontSize: 18, lineHeight: 1, color: 'inherit', marginLeft: 12 }}>
                    ×
                </button>
            )}
        </div>
    );
}

