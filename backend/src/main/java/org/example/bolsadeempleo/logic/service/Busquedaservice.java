package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.data.PuestoRepository;
import org.example.bolsadeempleo.logic.Puesto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Busquedaservice {

    @Autowired
    private PuestoRepository puestoRepository;

    @Transactional(readOnly = true)
    public List<Puesto> getUltimos5PuestosPublicos(){
        List<Puesto> puestos = puestoRepository.findTop5ByActivoAndPublicaOrderByFechaPublicacionDesc(true, true);
        // inicializar relaciones lazy dentro de la transacción
        puestos.forEach(p -> {
            if (p.getEmpresa() != null) p.getEmpresa().getNombre();
            p.getPuestoCaracteristicas().forEach(pc -> {
                if (pc.getCaracteristica() != null) pc.getCaracteristica().getId();
            });
        });
        return puestos;
    }

    @Transactional(readOnly = true)
    public List<Puesto> buscarPuestosPublicos(String palabra, BigDecimal salarioMin, Long caracteristica){
        List<Puesto> puestos = puestoRepository.findByActivoAndPublica(true, true);
        // inicializar relaciones lazy dentro de la transacción
        puestos.forEach(p -> {
            if (p.getEmpresa() != null) p.getEmpresa().getNombre();
            p.getPuestoCaracteristicas().forEach(pc -> {
                if (pc.getCaracteristica() != null) pc.getCaracteristica().getId();
            });
        });
        return puestos.stream()
                .filter(p -> palabra == null || palabra.isBlank() ||
                        (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(palabra.toLowerCase())))
                .filter(p -> salarioMin == null || p.getSalario() != null && p.getSalario().compareTo(salarioMin) >= 0)
                .filter(p -> caracteristica == null || p.getPuestoCaracteristicas().stream()
                        .anyMatch(pc -> pc.getCaracteristica() != null && pc.getCaracteristica().getId().equals(caracteristica)))
                .collect(Collectors.toList());
    }
}
