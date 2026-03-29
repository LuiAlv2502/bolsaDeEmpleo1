package org.example.bolsadeempleo.logic.service;


import org.example.bolsadeempleo.data.*;
import org.example.bolsadeempleo.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private PuestoRepository puestoRepository;

    public Administrador login(String identificacion, String clave){
        return administradorRepository.findByIdentificacionAndPassword(identificacion, clave).orElse(null);
    }

    public List<Empresa> getEmpresasPendientes() {
        return empresaRepository.findByAprobada(false);
    }

    public boolean autorizarEmpresa(Long id){
        Optional<Empresa> empresa = empresaRepository.findById(id);
        if(empresa.isEmpty()) return false;
        empresa.get().setAprobada(true);
        empresaRepository.save(empresa.get());
        return true;
    }

    public List<Oferente> getOferentesPendientes() {
        return oferenteRepository.findByAprobado(false);
    }
    public boolean autorizarOferente(String id){
        Optional<Oferente> oferente = oferenteRepository.findByIdentificacion(id);
        if(oferente.isEmpty()) return false;
        oferente.get().setAprobado(true);
        oferenteRepository.save(oferente.get());
        return true;
    }
    public List<Caracteristica> getCaracteristicasRaiz() {
        return caracteristicaRepository.findByParentIsNull();
    }
    public Caracteristica registrarCaracteristica(String nombre, Long padreId){
        Caracteristica caracteristica = new  Caracteristica();
        caracteristica.setNombre(nombre);

        if(padreId != null){
            caracteristicaRepository.findById(padreId).ifPresent(caracteristica::setParent);
        }
        return caracteristicaRepository.save(caracteristica);
    }
    public Optional<Caracteristica> obtenerCaracteristica(Long id){
        return caracteristicaRepository.findById(id);
    }
    public boolean eliminarCaracteristica(Long id){
        if(!caracteristicaRepository.existsById(id)) return false;
        caracteristicaRepository.deleteById(id);
        return true;
    }
    public List<Caracteristica> listarHijos(Long padreId){
        return caracteristicaRepository.findByParentId(padreId);
    }
    public List<Caracteristica> getRuta(Long Id){
        List<Caracteristica> ruta = new ArrayList<>();
        Optional<Caracteristica> actual = caracteristicaRepository.findById(Id);
        while (actual.isPresent() && actual.get().getParent() != null){
            ruta.add(0, actual.get());
            actual = Optional.ofNullable(actual.get().getParent());
        }
        return ruta;
    }
    public List<Puesto> puestosPorMes(int mes, int annio){
        YearMonth annioMes = YearMonth.of(annio, mes);
        Instant desde = annioMes.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant hasta = annioMes.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return puestoRepository.findByFechaPublicacionBetween(desde, hasta);
    }

    public Object todosLosPuestos() {
        return puestoRepository.findAll();
    }




}
