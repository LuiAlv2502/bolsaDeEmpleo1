package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.logic.Administrador;
import org.example.bolsadeempleo.logic.Caracteristica;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.data.AdministradorRepository;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.example.bolsadeempleo.data.EmpresaRepository;
import org.example.bolsadeempleo.data.OferenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdministradorRepository adminRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    public Administrador login(String identificacion, String clave) {
        return adminRepository.findByIdentificacionAndPassword(identificacion, clave).orElse(null);
    }

    // ── EMPRESAS ──────────────────────────────────────────────────────────────

    public List<Empresa> listarEmpresasPendientes() {
        return empresaRepository.findByAprobada(false);
    }

    public boolean autorizarEmpresa(Long id) {
        Optional<Empresa> empresa = empresaRepository.findById(id);
        if (empresa.isEmpty()) return false;
        empresa.get().setAprobado(true);
        empresaRepository.save(empresa.get());
        return true;
    }

    // ── OFERENTES ─────────────────────────────────────────────────────────────

    public List<Oferente> listarOferentesPendientes() {
        return oferenteRepository.findByAprobado(false);
    }

    public boolean autorizarOferente(String identificacion) {
        Optional<Oferente> oferente = oferenteRepository.findByIdentificacion(identificacion);
        if (oferente.isEmpty()) return false;
        oferente.get().setAprobado(true);
        oferenteRepository.save(oferente.get());
        return true;
    }

    // ── CARACTERÍSTICAS ───────────────────────────────────────────────────────

    public List<Caracteristica> listarCaracteristicasRaiz() {
        return caracteristicaRepository.findByParentIsNull();
    }

    public List<Caracteristica> listarTodasCaracteristicas() {
        return caracteristicaRepository.findAll();
    }

    public Caracteristica registrarCaracteristica(String nombre, Long padreId) {
        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setNombre(nombre);

        if (padreId != null) {
            caracteristicaRepository.findById(padreId)
                    .ifPresent(caracteristica::setParent); // ← CORREGIDO: setParent en vez de setPadre
        }

        return caracteristicaRepository.save(caracteristica);
    }

    public Optional<Caracteristica> obtenerCaracteristica(Long id) {
        return caracteristicaRepository.findById(id);
    }

    public boolean eliminarCaracteristica(Long id) {
        if (!caracteristicaRepository.existsById(id)) return false;
        caracteristicaRepository.deleteById(id);
        return true;
    }
}