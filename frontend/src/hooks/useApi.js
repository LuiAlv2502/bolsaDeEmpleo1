import { useState, useEffect, useCallback } from 'react';
import { apiGet } from '../api';

/**
 * Hook para GET con estado de carga y error automático.
 * Uso:
 *   const { data, loading, error, reload } = useApi('/api/publico/puestos/buscar');
 */
export function useApi(url) {
    const [data, setData]       = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);

    const fetch = useCallback(async () => {
        setLoading(true);
        setError(null);
        const res = await apiGet(url);
        if (res.ok) {
            setData(res.data);
        } else {
            setError(res.data?.error || `Error ${res.status}`);
        }
        setLoading(false);
    }, [url]);

    useEffect(() => { fetch(); }, [fetch]);

    return { data, loading, error, reload: fetch };
}

