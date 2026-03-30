package org.example.bolsadeempleo.logic.service;


import org.example.bolsadeempleo.data.*;
import org.example.bolsadeempleo.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registrar(Empresa empresa){
        if(empresaRepository.existsByCorreo(empresa.getCorreo())) return false;
        String hash = passwordEncoder.encode(empresa.getPassword());
        empresa.setPassword(hash);

        empresaRepository.save(empresa);
        return true;
    }
    public Empresa login(String correo, String clave){
        Optional<Empresa> empresa = empresaRepository.findByCorreo(correo);
        if(empresa.isEmpty()) return null;
        if(!passwordEncoder.matches(clave, empresa.get().getClave())) return null;
        if(!empresa.get().isAprobado())return null;
        return empresa.get();
    }
    public Empresa getById(Long id){
        return empresaRepository.findById(id).orElse(null);
    }
    public Puesto publicarPuesto(Long empresaId, String descr, BigDecimal salario, boolean publico,
                                 String moneda, List<Long> caracteristicasIds, List<Integer> niveles){
        Optional<Empresa> empresa = empresaRepository.findById(empresaId);
        if(empresa.isEmpty()) return null;

        Puesto puesto = new Puesto();
        puesto.setEmpresa(empresa.get());
        puesto.setDescripcion(descr);
        puesto.setSalario(salario);
        puesto.setMoneda(moneda);
        puesto.setPublica(publico);
        puesto.setActivo(true);
        puestoRepository.save(puesto);

        for(int i = 0; i < caracteristicasIds.size(); i++){
            Long caracId = caracteristicasIds.get(i);
            Optional<Caracteristica> caracteristica = caracteristicaRepository.findById(caracId);
            if (caracteristica.isEmpty()) return null;

            PuestoCaracteristica requisito = new PuestoCaracteristica();
            requisito.setPuesto(puesto);
            requisito.setCaracteristica(caracteristica.get());
            requisito.setNivelRequerido(niveles.get(i));
            requisitoPuestoRepository.save(requisito);
        }
        return puesto;
    }
    public boolean desactivarPuesto(Long puestoId){
        Optional<Puesto> puesto = puestoRepository.findById(puestoId);
        if(puesto.isEmpty()) return false;

        puesto.get().setActivo(false);
        puestoRepository.save(puesto.get());
        return true;
    }
    public List<Puesto> getPuestosPorEmpresa(Long empresaId){
        return puestoRepository.findByEmpresaId(empresaId);
    }
    @Transactional(readOnly = true)
    public List<ResultadoBusquedaOferente> buscarCandidatos(Long empresaId){
        List<PuestoCaracteristica> requisitos = requisitoPuestoRepository.findByPuestoId(empresaId);
        if (requisitos.isEmpty()) return null;

        List<Oferente> todosOferentes = oferenteRepository.findByAprobado(true);
        List<ResultadoBusquedaOferente> candidatos = new ArrayList<>();

        for(Oferente oferente : todosOferentes){
            double puntuacion = calcularPuntuacion(oferente.getIdentificacion(), requisitos);
            if(puntuacion > 0){
                double porcentaje = (puntuacion/ requisitos.size()) * 100;
                double porcentajeRedondeado = BigDecimal.valueOf(porcentaje).setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                candidatos.add(new ResultadoBusquedaOferente(oferente, puntuacion, requisitos.size(), porcentajeRedondeado));
            }
        }
        candidatos.sort((a, b) -> Double.compare(b.getPorcentaje(), a.getPorcentaje()));
        return candidatos;
    }
    private double calcularPuntuacion(String identificacion, List<PuestoCaracteristica> requisitos){
        double total = 0;
        for (PuestoCaracteristica requisito : requisitos) {
            Long caracId = requisito.getCaracteristica().getId();
            Optional<Habilidad> habilidad = habilidadRepository.findByOferente_IdentificacionAndCaracteristica_Id(identificacion, caracId);
            if(habilidad.isPresent()){
                int nivelOferente = habilidad.get().getNivel();
                int nivelRequerido = requisito.getNivelRequerido();
                if (nivelOferente >= nivelRequerido){
                    total += 1.0;
                }else{
                    total += 0.5;
                }
            }
        }
        return total;
    }
    public Puesto getPuesto(Long puestoId){
        return puestoRepository.findById(puestoId).orElse(null);
    }
}
