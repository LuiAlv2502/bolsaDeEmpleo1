import { HashRouter, Routes, Route, Link } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import ScrollToTop from './components/ScrollToTop';
import Inicio from './pages/Inicio';
import Login from './pages/Login';
import EmpresaRegistro from './pages/empresa/EmpresaRegistro';
import EmpresaDashboard from './pages/empresa/EmpresaDashboard';
import EmpresaPuestos from './pages/empresa/EmpresaPuestos';
import EmpresaDetallePuesto from './pages/empresa/EmpresaDetallePuesto';
import OferenteRegistro from './pages/oferente/OferenteRegistro';
import OferenteDashboard from './pages/oferente/OferenteDashboard';
import OferenteHabilidades from './pages/oferente/OferenteHabilidades';
import OferenteCV from './pages/oferente/OferenteCV';
import OferenteBusqueda from './pages/oferente/OferenteBusqueda';

import AdminPanel from './pages/admin/AdminPanel';
import './index.css';
import './auth.css';

export default function App() {
    return (
        <AuthProvider>
            <HashRouter>
                <ScrollToTop />
                <header>
                    <h1><Link to="/" style={{ color: 'inherit', textDecoration: 'none' }}>Bolsa de Empleo</Link></h1>
                    <Navbar />
                </header>

                <div id="app">
                    <Routes>
                        {/* Rutas públicas */}
                        <Route path="/" element={<Inicio />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/empresa/registro" element={<EmpresaRegistro />} />
                        <Route path="/oferente/registro" element={<OferenteRegistro />} />

                        {/* Rutas protegidas - Empresa */}
                        <Route path="/empresa/dashboard" element={
                            <ProtectedRoute roles={['empresa']}><EmpresaDashboard /></ProtectedRoute>
                        } />
                        <Route path="/empresa/puestos" element={
                            <ProtectedRoute roles={['empresa']}><EmpresaPuestos /></ProtectedRoute>
                        } />
                        <Route path="/empresa/puestos/:id" element={
                            <ProtectedRoute roles={['empresa']}><EmpresaDetallePuesto /></ProtectedRoute>
                        } />

                        {/* Rutas protegidas - Oferente */}
                        <Route path="/oferente/dashboard" element={
                            <ProtectedRoute roles={['oferente']}><OferenteDashboard /></ProtectedRoute>
                        } />
                        <Route path="/oferente/habilidades" element={
                            <ProtectedRoute roles={['oferente']}><OferenteHabilidades /></ProtectedRoute>
                        } />
                        <Route path="/oferente/cv" element={
                            <ProtectedRoute roles={['oferente']}><OferenteCV /></ProtectedRoute>
                        } />
                        <Route path="/oferente/busqueda" element={
                            <ProtectedRoute roles={['oferente']}><OferenteBusqueda /></ProtectedRoute>
                        } />


                        {/* Rutas protegidas - Admin */}
                        <Route path="/admin/panel" element={
                            <ProtectedRoute roles={['admin']}><AdminPanel /></ProtectedRoute>
                        } />
                    </Routes>
                </div>

                <footer>
                    <p>2026 Bolsa de Empleo. Todos los derechos reservados.</p>
                </footer>
            </HashRouter>
        </AuthProvider>
    );
}
