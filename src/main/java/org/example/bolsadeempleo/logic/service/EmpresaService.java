package org.example.bolsadeempleo.logic.service;


import org.example.bolsadeempleo.logic.*;
import org.example.bolsadeempleo.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.bolsadeempleo.logic.service.ResultadoBusquedaOferente;
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

    public boolean registrar(Empresa empresa){
        if (empresaRepository.existsById(empresa.getId())) return false;
        String hashPass = passwordEncoder.encode(empresa.getPassword());
        empresa.setPassword(hashPass);

        empresaRepository.save(empresa);
        return true;
    }
    public Empresa login(String correo, String clave){
        Optional<Empresa> empresa = empresaRepository.findByCorreo(correo);
        if(empresa.isEmpty()) return null;
        if(!passwordEncoder.matches(clave, empresa.get().getClave())) return null;
        if(!empresa.get().isAprobado()) return null;
        return empresa.get();
    }
    public Empresa obtenerPorId(Long id){
        return empresaRepository.findById(id).orElse(null);
    }
    public Puesto publicarPuesto(Long empresaId, String descripcion, BigDecimal salario,
                                 boolean publica, String moneda, List<Long> caracteristicasIds, List<Integer> niveles){
        Optional<Empresa> empresa = empresaRepository.findById(empresaId);
        if(empresa.isEmpty()){
            return null;
        }

        Puesto puesto = new Puesto();
        puesto.setEmpresa(empresa.get());
        puesto.setDescripcion(descripcion);
        puesto.setSalario(salario);
        puesto.setPublica(publica);
        puesto.setMoneda(moneda);
        puesto.setActivo(true);
        puestoRepository.save(puesto);

        for(int i = 0; i < caracteristicasIds.size(); i++){
            Long caracId = caracteristicasIds.get(i);
            Optional<Caracteristica> caracteristica = caracteristicaRepository.findById(caracId);
            if(caracteristica.isEmpty()) continue;

            PuestoCaracteristica requisito = new  PuestoCaracteristica();
            requisito.setPuesto(puesto);
            requisito.setCaracteristica(caracteristica.get());
            requisito.setNivelRequerido(niveles.get(i));
            requisitoPuestoRepository.save(requisito);

        }
        return puesto;

    }
    public boolean desactivarPuesto(Long puestoId, Long empresaId){
        Optional<Puesto> puesto = puestoRepository.findById(puestoId);
        if(puesto.isEmpty()) return false;

        puesto.get().setActivo(false);
        puestoRepository.save(puesto.get());
        return true;
    }
    public List<Puesto> listarPuestosPorEmpresa(Long empresaId){
        return puestoRepository.findByEmpresaId(empresaId);
    }
    private double calcularPuntuacion(String identificacion, List<PuestoCaracteristica> requisitos){
        double puntuacion = 0.0;
        for(PuestoCaracteristica requisito : requisitos){
            Long caracId = requisito.getCaracteristica().getId();
            Optional<Habilidad> habilidad = habilidadRepository.findByOferente_IdentificacionAndCaracteristica_Id(identificacion, caracId);

            if(habilidad.isPresent()){
                int nivelOferente = habilidad.get().getNivel();
                int nivelRequerido = requisito.getNivelRequerido();
                if(nivelOferente >= nivelRequerido){
                    puntuacion += 1.0;

                }else{
                    puntuacion += 0.5;
                }
            }
        }
        return puntuacion;
    }


    @Transactional(readOnly = true)
    public List<ResultadoBusquedaOferente> buscarCandidatos(Long puestoId){
        List<PuestoCaracteristica> requisitos = requisitoPuestoRepository.findByPuestoId(puestoId);
        if(requisitos.isEmpty()) return new ArrayList<>();

        List<Oferente> todosOferentes = oferenteRepository.findByAprobado(true);
        List<ResultadoBusquedaOferente> candidatos = new ArrayList<>();

        for(Oferente oferente : todosOferentes){
            double puntuacion = calcularPuntuacion(oferente.getIdentificacion(), requisitos);
            if(puntuacion > 0){
                double porcentaje = (puntuacion/ requisitos.size()) * 100;
                double porcentajeRedondeado = BigDecimal.valueOf(porcentaje)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                candidatos.add(new ResultadoBusquedaOferente(oferente, puntuacion, requisitos.size(), porcentajeRedondeado));
            }
        }
        candidatos.sort((a, b) -> Double.compare(b.getPorcentaje(), a.getPorcentaje()));
        return candidatos;
    }

    public Puesto obtenerPuesto(Long puestoId) {
        return puestoRepository.findById(puestoId).orElse(null);
    }

    // ── CLASE WRAPPER PARA RESULTADOS DE BÚSQUEDA ─────────────────────────────


}