package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.logic.*;
import org.example.bolsadeempleo.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

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
    private BCryptPasswordEncoder passwordEncoder;

    // REGISTRO
    public boolean registrar(Empresa empresa) {
        if (empresaRepository.existsByCorreo(empresa.getCorreo())) return false;
        empresa.setClave(passwordEncoder.encode(empresa.getClave()));
        empresaRepository.save(empresa);
        return true;
    }

    // LOGIN
    public Empresa login(String correo, String clave) {
        Optional<Empresa> empresa = empresaRepository.findByCorreo(correo);
        if (empresa.isEmpty()) return null;
        boolean claveCorrecta = passwordEncoder.matches(clave, empresa.get().getClave());
        if (!claveCorrecta) return null;
        if (!empresa.get().isAprobado()) return null;
        return empresa.get();
    }

    // ACTUALIZAR DATOS
    public boolean actualizarDatos(Empresa datosActualizados) {
        Optional<Empresa> existente = empresaRepository.findById(datosActualizados.getId());
        if (existente.isEmpty()) return false;

        Empresa empresa = existente.get();
        empresa.setNombre(datosActualizados.getNombre());
        empresa.setLocalizacion(datosActualizados.getLocalizacion());
        empresa.setCorreo(datosActualizados.getCorreo());
        empresa.setTelefono(datosActualizados.getTelefono());
        empresa.setDescripcion(datosActualizados.getDescripcion());

        empresaRepository.save(empresa);
        return true;
    }

    // PUESTOS - PUBLICAR

    // PUESTOS - PUBLICAR
    public Puesto publicarPuesto(Long empresaId, String descripcion, BigDecimal salario,
                                 boolean publica, List<Long> caracteristicaIds, List<Integer> niveles) {
        Optional<Empresa> empresa = empresaRepository.findById(empresaId);
        if (empresa.isEmpty()) return null;

        Puesto puesto = new Puesto();
        puesto.setEmpresa(empresa.get());
        puesto.setDescripcion(descripcion);
        puesto.setSalario(salario);
        puesto.setPublica(publica);
        puesto.setActivo(true);
        puestoRepository.save(puesto);

        for (int i = 0; i < caracteristicaIds.size(); i++) {
            PuestoCaracteristica requisito = new PuestoCaracteristica();
            requisito.setPuesto(puesto);
            requisito.setId(caracteristicaIds.get(i));
            requisito.setNivelRequerido(niveles.get(i));
            requisitoPuestoRepository.save(requisito);
        }

        return puesto;
    }

    // PUESTOS - DESACTIVAR
    public boolean desactivarPuesto(Long puestoId, Long empresaId) {
        Optional<Puesto> puesto = puestoRepository.findById(puestoId);
        if (puesto.isEmpty()) return false;
        if (!puesto.get().getEmpresa().getId().equals(empresaId)) return false;

        puesto.get().setActivo(false);
        puestoRepository.save(puesto.get());
        return true;
    }

    // PUESTOS - LISTAR POR EMPRESA
    public List<Puesto> listarPuestosPorEmpresa(Long empresaId) {
        return puestoRepository.findByEmpresaId(empresaId);
    }

    // BUSCAR CANDIDATOS
    public List<Oferente> buscarCandidatos(Long puestoId) {
        List<PuestoCaracteristica> requisitos = requisitoPuestoRepository.findByPuestoId(puestoId);
        if (requisitos.isEmpty()) return new ArrayList<>();

        List<Oferente> todosOferentes = oferenteRepository.findByAprobado(true);
        List<Oferente> candidatos = new ArrayList<>();

        for (Oferente oferente : todosOferentes) {
            if (cumpleRequisitos(oferente.getIdentificacion(), requisitos)) {
                candidatos.add(oferente);
            }
        }

        return candidatos;
    }

    private boolean cumpleRequisitos(String identificacion, List<PuestoCaracteristica> requisitos) {
        for (PuestoCaracteristica requisito : requisitos) {
            Optional<Habilidad> habilidad = habilidadRepository
                    .findByOferenteIdentificacionAndCaracteristicaId(
                            identificacion, requisito.getId());

            if (habilidad.isEmpty()) return false;
            if (habilidad.get().getNivel() < requisito.getNivelRequerido()) return false;
        }
        return true;
    }

    // VER DETALLE CANDIDATO
    public Oferente verDetalleCandidato(String identificacion) {
        return oferenteRepository.findById(identificacion).orElse(null);
    }

//    // VER CURRICULUM CANDIDATO
//    public byte[] obtenerCurriculumCandidato(String identificacion) {
//        return oferenteRepository.findById(identificacion)
//                .map(Oferente::getCurriculum)
//                .orElse(null);
//    }
}