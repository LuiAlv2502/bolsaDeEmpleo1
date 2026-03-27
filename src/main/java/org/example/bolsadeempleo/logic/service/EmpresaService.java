package org.example.bolsadeempleo.logic.service;

import lombok.Getter;
import org.example.bolsadeempleo.logic.*;
import org.example.bolsadeempleo.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PuestoRepository puestoRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private HabilidadRepository habilidadRepository;

    @Autowired
    private RequisitoPuestoRepository requisitoPuestoRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ── REGISTRO ──────────────────────────────────────────────────────────────

    public boolean registrar(Empresa empresa) {
        if (empresaRepository.existsByCorreo(empresa.getCorreo())) return false;
        
        String hashPass = passwordEncoder.encode(empresa.getPassword());
        empresa.setPassword(hashPass);

        empresaRepository.save(empresa);
        return true;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    public Empresa login(String correo, String clave) {
        Optional<Empresa> empresa = empresaRepository.findByCorreo(correo);
        if (empresa.isEmpty()) return null;
        if (!passwordEncoder.matches(clave, empresa.get().getClave())) return null;
        if (!empresa.get().isAprobado()) return null;
        return empresa.get();
    }

    // ── OBTENER POR ID ────────────────────────────────────────────────────────

    public Empresa obtenerPorId(Long id) {
        return empresaRepository.findById(id).orElse(null);
    }

    // ── ACTUALIZAR DATOS ──────────────────────────────────────────────────────



    // ── PUBLICAR PUESTO ───────────────────────────────────────────────────────

    /**
     * BUG CORREGIDO: antes se hacía requisito.setId(caracteristicaIds.get(i))
     * lo cual sobreescribía el PK del registro en vez de asignar la característica.
     * Ahora se crea una referencia proxy de Caracteristica con solo el id.
     */
    public Puesto publicarPuesto(Long empresaId, String descripcion, BigDecimal salario,
                                 boolean publica, String moneda,
                                 List<Long> caracteristicaIds, List<Integer> niveles) {
        Optional<Empresa> empresa = empresaRepository.findById(empresaId);
        if (empresa.isEmpty()) return null;

        Puesto puesto = new Puesto();
        puesto.setEmpresa(empresa.get());
        puesto.setDescripcion(descripcion);
        puesto.setSalario(salario);
        puesto.setPublica(publica);
        puesto.setMoneda(moneda != null ? moneda : "CRC");
        puesto.setActivo(true);
        puestoRepository.save(puesto);

        for (int i = 0; i < caracteristicaIds.size(); i++) {
            Long caracId = caracteristicaIds.get(i);
            Optional<Caracteristica> caracteristica = caracteristicaRepository.findById(caracId);
            if (caracteristica.isEmpty()) continue; // ignorar ids inválidos

            PuestoCaracteristica requisito = new PuestoCaracteristica();
            requisito.setPuesto(puesto);
            requisito.setCaracteristica(caracteristica.get()); // ← CORREGIDO
            requisito.setNivelRequerido(niveles.get(i));
            requisitoPuestoRepository.save(requisito);
        }

        return puesto;
    }


    public boolean desactivarPuesto(Long puestoId, Long empresaId) {
        Optional<Puesto> puesto = puestoRepository.findById(puestoId);
        if (puesto.isEmpty()) return false;
        if (!puesto.get().getEmpresa().getId().equals(empresaId)) return false;

        puesto.get().setActivo(false);
        puestoRepository.save(puesto.get());
        return true;
    }

    // ── LISTAR PUESTOS POR EMPRESA ────────────────────────────────────────────

    public List<Puesto> listarPuestosPorEmpresa(Long empresaId) {
        return puestoRepository.findByEmpresaId(empresaId);
    }

    public List<Puesto> listarPuestosActivos(Long empresaId) {
        return puestoRepository.findByEmpresaIdAndActivo(empresaId, true);
    }

    // ── BUSCAR CANDIDATOS ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CandidatoDTO> buscarCandidatos(Long puestoId){
        List<PuestoCaracteristica> requisitos = requisitoPuestoRepository.findByPuestoId(puestoId);
        if(requisitos.isEmpty()) return new  ArrayList<>();

        List<Oferente> todosOferentes = oferenteRepository.findByAprobado(true);
        List<CandidatoDTO> candidatos = new ArrayList<>();

        for (Oferente oferente : todosOferentes) {
            double puntuacion = calcularPuntuacion(oferente.getIdentificacion(), requisitos);
            if (puntuacion > 0){
                double porcentaje = (puntuacion/ requisitos.size()) * 100.0;
                candidatos.add(new CandidatoDTO(oferente, puntuacion, requisitos.size(), porcentaje));
            }
        }
        candidatos.sort((a, b) -> Double.compare(b.getPorcentaje(), a.getPorcentaje()));
        return candidatos;
    }

    private double calcularPuntuacion(String identificacion,  List<PuestoCaracteristica> requisitos) {
        double puntuacion = 0.0;
        for(PuestoCaracteristica requisito : requisitos) {
            Long caracId = requisito.getCaracteristica().getId();
            Optional<Habilidad> habilidad = habilidadRepository.findByOferente_IdentificacionAndCaracteristica_Id(identificacion, caracId);

            if(habilidad.isPresent()) {
                int nivelOferente = habilidad.get().getNivel();
                int nivelRequerido = requisito.getNivelRequerido();

                if(nivelOferente >= nivelRequerido) {
                    puntuacion += 1.0;
                }else{
                    puntuacion += 0.5;
                }

            }
        }
        return puntuacion;
    }

    // ── VER DETALLE CANDIDATO ─────────────────────────────────────────────────

    public Oferente verDetalleCandidato(String identificacion) {
        return oferenteRepository.findByIdentificacion(identificacion).orElse(null);
    }

    // ── OBTENER PUESTO ────────────────────────────────────────────────────────

    public Puesto obtenerPuesto(Long puestoId) {
        return puestoRepository.findById(puestoId).orElse(null);
    }

    public List<PuestoCaracteristica> obtenerRequisitos(Long puestoId) {
        return requisitoPuestoRepository.findByPuestoId(puestoId);
    }

    // ── DTO CANDIDATO ─────────────────────────────────────────────────────────

    public static class CandidatoDTO {
        @Getter
        private final Oferente oferente;
        @Getter
        private final double puntuacion;
        @Getter
        private final int    requisitosTotal;
        private final double porcentaje;

        public CandidatoDTO(Oferente oferente, double puntuacion, int total, double porcentaje) {
            this.oferente        = oferente;
            this.puntuacion      = puntuacion;
            this.requisitosTotal = total;
            this.porcentaje      = porcentaje;
        }

        public double getPorcentaje() {
            return BigDecimal.valueOf(porcentaje)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        public String getPorcentajeFormateado() {
            return String.format("%.2f%%", porcentaje);
        }
    }

}