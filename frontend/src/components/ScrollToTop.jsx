import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

/** Hace scroll al top cada vez que cambia la ruta */
export default function ScrollToTop() {
    const { pathname } = useLocation();
    useEffect(() => { window.scrollTo(0, 0); }, [pathname]);
    return null;
}

