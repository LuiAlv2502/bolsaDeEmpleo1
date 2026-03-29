package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.data.PuestoRepository;
import org.example.bolsadeempleo.logic.Puesto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Busquedaservice {

    @Autowired
    private PuestoRepository puestoRepository;

    public List<Puesto> getUltimos5PuestosPublicos(){
        return puestoRepository.findTop5ByActivoAndPublicaOrderByFechaPublicacionDesc(true, true);
    }

    public List<Puesto> buscarPuestosPublicos(String palabra, BigDecimal salarioMin, Long caracteristica){
        List<Puesto> puestos = puestoRepository.findByActivoAndPublica(true, true);
        return puestos.stream().filter(p-> palabra == null || palabra.isBlank() ||
                p.getDescripcion().toLowerCase().contains(palabra.toLowerCase()))
                .filter(p-> salarioMin == null || p.getSalario().compareTo(salarioMin) >= 0)
                .filter(p -> caracteristica == null || p.getPuestoCaracteristicas().stream().anyMatch(pc -> pc.getCaracteristica().getId().equals(caracteristica)))
                .collect(Collectors.toList());
    }
}
