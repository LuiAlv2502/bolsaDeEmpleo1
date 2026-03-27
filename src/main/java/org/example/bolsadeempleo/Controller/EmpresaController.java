package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
//listo
@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private OferenteService oferenteService;
    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresa/registro";
    }

    @PostMapping("/registro")
    public String registro(@ModelAttribute("empresa") Empresa empresa, @RequestParam("password") String password,
                           @RequestParam("confirmarPassword") String confirmarPassword, Model model) {

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "empresa/registro";

        } else if (password.length() < 8) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "empresa/registro";

        }
        if (empresa.getNombre() == null || empresa.getNombre().isEmpty()) {
            model.addAttribute("error", "El nombre es obligatorio.");
            return "empresa/registro";
        }
        if (empresa.getCorreo() == null || empresa.getCorreo().isEmpty()) {
            model.addAttribute("error", "El correo es obligatorio.");
            return "empresa/registro";
        }
        empresa.setClave(password);
        empresa.setAprobado(false);

        if (!empresaService.registrar(empresa)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo.");
            return "empresa/registro";
        }

        model.addAttribute("success", "Se logró registrar la empresa");
        model.addAttribute("empresa", new Empresa());
        return "empresa/registro";
    }


    private Long getEmpresaId(HttpSession session) {
        Object id = session.getAttribute("empresaId");
        if (id == null) {
            return null;
        }
        return (Long) id;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) {
            return "redirect:/login";
        }
        model.addAttribute("nombre", session.getAttribute("empresaNombre"));
        return "empresa/dashboard";

    }

    @GetMapping("/puestos")
    public String puestos(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);

        model.addAttribute("puestos", empresaService.listarPuestosPorEmpresa(empresaId));
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        model.addAttribute("nombre", session.getAttribute("empresaNombre"));
        return "empresa/Puestos";

    }

    @PostMapping("/publicarPuesto")
    public String publicarPuesto(HttpSession session,
                                 @RequestParam("descripcion") String descripcion,
                                 @RequestParam("salario") BigDecimal salario,
                                 @RequestParam("publica") boolean publica,
                                 @RequestParam(value = "caracteristicaIds", required = false) List<String> caracteristicaIdsStr,
                                 @RequestParam(value = "niveles", required = false) List<Integer> niveles,
                                 @RequestParam("moneda") String moneda,
                                 RedirectAttributes redirectAttrs) {
        List<Long> caracteristicaIds = new ArrayList<>();
        List<Integer> nivelesValidos = new ArrayList<>();
        if (caracteristicaIdsStr != null) {
            for (int i = 0; i < caracteristicaIdsStr.size(); i++) {
                String idStr = caracteristicaIdsStr.get(i);
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        caracteristicaIds.add(Long.parseLong(idStr));
                        nivelesValidos.add(niveles != null && i < niveles.size() ? niveles.get(i) : 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        Long empresaId = getEmpresaId(session);
        Puesto puesto = empresaService.publicarPuesto(empresaId, descripcion, salario, publica, moneda, caracteristicaIds, nivelesValidos);

        if(puesto == null) {
            redirectAttrs.addFlashAttribute("error", "No se pudo publicar el puesto");
            return "redirect:/empresa/puestos";
        }
        redirectAttrs.addFlashAttribute("succes", "Puesto publicado exitosamente");
        return  "redirect:/empresa/puestos";

    }

    @GetMapping("/puestos/{id}/detalle")
    public String detallePuesto(@PathVariable Long id, HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        Puesto puesto = empresaService.obtenerPuesto(id);
        if(puesto == null || !puesto.getEmpresa().getId().equals(empresaId)) {
            return "redirect:/empresa/puestos";
        }

        model.addAttribute("puesto", puesto);
        model.addAttribute("nombre", session.getAttribute("empresaNombre"));
        model.addAttribute("candidatos", java.util.Collections.emptyList());
        return "empresa/detalle-puesto";
    }

    @PostMapping("/puestos/{id}/desactivar")
    public String desactivarPuesto(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttrs) {
        Long empresaId = getEmpresaId(session);
        empresaService.desactivarPuesto(empresaId, id);
        redirectAttrs.addFlashAttribute("succes", "Puesto desactivado");
        return "redirect:/empresa/puestos";

    }
    @GetMapping("/candidatos/buscar")
    public String buscarCandidatos(@RequestParam("puestoId") Long puestoId, HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        Puesto puesto = empresaService.obtenerPuesto(puestoId);

        model.addAttribute("puesto", puesto);
        model.addAttribute("candidatos", empresaService.buscarCandidatos(puestoId));
        return "empresa/buscar-candidatos";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        model.addAttribute("empresa", empresaService.obtenerPorId(empresaId));
        return "empresa/perfil";

    }




}
