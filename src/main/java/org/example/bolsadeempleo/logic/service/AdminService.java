package org.example.bolsadeempleo.logic.service;

import jakarta.persistence.Id;
import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.data.*;
import org.example.bolsadeempleo.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService{
    @Autowired
    private AdministradorRepository adminRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private PuestoRepository puestoRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    //Login

    public Administrador login(String identificacion,String clave){
        return adminRepository.findByIdentificacionAndPassword(identificacion,clave).orElse(null);
    }

    //Empresas

    public List<Empresa> listaEmpresasPendientes(){
        return empresaRepository.findByAprobada(false);
    }

    public boolean autorizarEmpresa(Long id){
        Optional<Empresa> empresa = empresaRepository.findById(id);
        if(empresa.isEmpty())return false;
        empresa.get().setAprobada(true);
        empresaRepository.save(empresa.get());
        return true;
    }

    //Oferentes

    public List<Oferente> listaOferentesPendientes(){
        return oferenteRepository.findByAprobado(false);
    }

    public boolean autorizarOferente(String identificacion){
        Optional<Oferente> oferente = oferenteRepository.findByIdentificacion(identificacion);
        if(oferente.isEmpty())return false;
        oferente.get().setAprobado(true);
        oferenteRepository.save(oferente.get());
        return true;
    }

    //Caracteristicas

    public List<Caracteristica> listaCaracteristicasRaiz(){
        return caracteristicaRepository.findByParentIsNull();
    }

    public  List<Caracteristica> listarTodasCaracteristicas(){
        return caracteristicaRepository.findAll();
    }

    public Caracteristica registrarCaracteristica(String nombre,Long padreId){
        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setNombre(nombre);

        if(padreId!=null){
            caracteristicaRepository.findById(padreId).ifPresent(caracteristica::setParent);
        };
        return caracteristicaRepository.save(caracteristica);
    }

    public Optional<Caracteristica> obtenerCaracteristaca(Long id){
        return caracteristicaRepository.findById(id);
    }

    public boolean eliminarCaracteristica(Long id){
        if(!caracteristicaRepository.existsById(id))return false;
        caracteristicaRepository.deleteById(id);
        return true;
    }

    public boolean tieneHijos(Long id){
        return caracteristicaRepository.existsByParent_Id(id);
    }

    //Puesto

    public List<Puesto> listarTodosPuestos(){
        return puestoRepository.findAll();
    }
}

