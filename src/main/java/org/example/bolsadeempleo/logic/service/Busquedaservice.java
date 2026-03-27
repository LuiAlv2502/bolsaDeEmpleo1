package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.data.PuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Busquedaservice {

    @Autowired
    private PuestoRepository puestoRepository;

    public List<Puesto> obtenerUltimosPuestosPublicos() {
        return puestoRepository.findUltimos5PuestosPublicos(true, true);
    }

    public List<Puesto> buscarPuestosPublicos(String palabraClave, BigDecimal salarioMin, Long caracteristica) {
        List<Puesto> todos = puestoRepository.findByActivoAndPublica(true, true);

        return todos.stream()
                .filter(p -> palabraClave == null || palabraClave.isBlank() ||
                        p.getDescripcion().toLowerCase().contains(palabraClave.toLowerCase()))
                .filter(p -> salarioMin == null ||
                        (p.getSalario() != null && p.getSalario().compareTo(salarioMin) >= 0))
                .filter(p -> caracteristica == null || p.getPuestoCaracteristicas().stream()
                        .anyMatch(pc -> pc.getCaracteristica().getId().equals(caracteristica)))
                .collect(Collectors.toList());
    }

}