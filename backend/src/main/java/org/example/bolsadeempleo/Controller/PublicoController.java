package org.example.bolsadeempleo.Controller;

import org.example.bolsadeempleo.logic.service.Busquedaservice;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;
    @Autowired
    private Busquedaservice busquedaservice;

    @GetMapping("/inicio")
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(Map.of(
                "puestosRecientes", busquedaservice.getUltimos5PuestosPublicos()
                //"caracteristicas", caracteristicaRepository.findAll()
        ));
    }

    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPuestos(
            @RequestParam(value = "palabra", required = false) String palabra,
            @RequestParam(value = "salarioMin", required = false) BigDecimal salarioMin,
            @RequestParam(value = "caracteristica", required = false) String caracteristica) {

        Long caracteristicaId = (caracteristica != null && !caracteristica.isBlank())
                ? Long.valueOf(caracteristica)
                : null;

        return ResponseEntity.ok(Map.of(
                "resultados", busquedaservice.buscarPuestosPublicos(palabra, salarioMin, caracteristicaId),
                "puestosRecientes", busquedaservice.getUltimos5PuestosPublicos(),
                "caracteristicas", caracteristicaRepository.findAll()
        ));
    }
}
