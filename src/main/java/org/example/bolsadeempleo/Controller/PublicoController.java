package org.example.bolsadeempleo.Controller;

import org.example.bolsadeempleo.logic.service.Busquedaservice;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class PublicoController {
    //
    @Autowired
    private CaracteristicaRepository caracteristicaRepository;
    @Autowired
    private Busquedaservice busquedaservice;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("puestosRecientes", busquedaservice.getUltimos5PuestosPublicos());
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        return "index";

    }
    @GetMapping("/puestos/buscar")
    public String buscarPuestos(@RequestParam(value = "palabra", required = false) String palabra,
                                @RequestParam(value = "salarioMin", required = false) BigDecimal salarioMin,
                                @RequestParam(value = "caracteristica", required = false) String caracteristica,
                                Model model) {

        Long caracteristicaId = (caracteristica != null && !caracteristica.isBlank())
                ? Long.valueOf(caracteristica)
                : null;

        model.addAttribute("resultados", busquedaservice.buscarPuestosPublicos(palabra, salarioMin, caracteristicaId));
        model.addAttribute("puestosRecientes", busquedaservice.getUltimos5PuestosPublicos());
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        model.addAttribute("palabra", palabra);
        model.addAttribute("salarioMin", salarioMin);
        model.addAttribute("caracteristicaId", caracteristicaId);
        return "index";
    }

}
