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

    @Autowired
    private Busquedaservice busquedaService;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("puestosRecientes",
                busquedaService.obtenerUltimosPuestosPublicos());
        model.addAttribute("caracteristicas",
                caracteristicaRepository.findAll());
        return "index";
    }

    @GetMapping("/puestos/buscar")
    public String buscarPuestos(
            @RequestParam(value = "palabraClave", required = false) String palabraClave,
            @RequestParam(value = "salarioMin", required = false) BigDecimal salarioMin,
            Model model) {

        model.addAttribute("resultados",
                busquedaService.buscarPuestosPublicos(palabraClave, salarioMin));
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        model.addAttribute("palabraClave", palabraClave);
        model.addAttribute("salarioMin", salarioMin);
        return "buscar-puestos";
    }
}