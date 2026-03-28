package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.data.PuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.List;

@Service
public class Busquedaservice {
    @Autowired
    private PuestoRepository puestoRepository;

    public List<Puesto> obtnerUltimosPuestosPublicos(){
        return puestoRepository.findTop5ByActivoAndPublicaOrderByFechaPublicacionDesc(true,true);
    }

    public List<Puesto> buscarPuestosPublicos(String palabraClave,BigDecimal salarioMin){
        List<Puesto>  todos = puestoRepository.findByActivoAndPublica(true,true);
        return todos.stream().filter(puesto -> palabraClave ==null || palabraClave.isBlank() || puesto.getDescripcion().toLowerCase().contains(palabraClave.toLowerCase())).filter(puesto -> salarioMin==null || (puesto.getSalario() != null && puesto.getSalario().compareTo(salarioMin)>=0)).collect(Collectors.toList());
    }

    public Puesto obtenerDetallePuesto(long id){
        return puestoRepository.findById(id).orElse(null);
    }
}
