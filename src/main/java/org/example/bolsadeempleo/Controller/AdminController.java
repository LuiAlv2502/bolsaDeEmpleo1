package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ==================== LOGIN (redirige al login unificado) ====================

    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    // ==================== PANEL ====================

    @GetMapping("/panel")
    public String panel(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("nombre", session.getAttribute("adminNombre"));
        model.addAttribute("empresasPendientes", adminService.listarEmpresasPendientes());
        model.addAttribute("oferentesPendientes", adminService.listarOferentesPendientes());
        model.addAttribute("caracteristicas", adminService.listarTodasCaracteristicas());

        return "admin/panel";
    }

    // ==================== APROBAR EMPRESA ====================

    @PostMapping("/empresa/aprobar/{id}")
    public String aprobarEmpresa(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/login";
        }
        adminService.autorizarEmpresa(id);
        return "redirect:/admin/panel";
    }

    // ==================== APROBAR OFERENTE ====================

    @PostMapping("/oferente/aprobar/{identificacion}")
    public String aprobarOferente(@PathVariable String identificacion, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/login";
        }
        adminService.autorizarOferente(identificacion);
        return "redirect:/admin/panel";
    }

    // ==================== CARACTERISTICAS ====================

    @PostMapping("/caracteristica/nueva")
    public String crearCaracteristica(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "padreId", required = false) Long padreId,
            HttpSession session) {

        if (session.getAttribute("adminId") == null) {
            return "redirect:/login";
        }

        if (nombre == null || nombre.isBlank()) {
            return "redirect:/admin/panel?errorCaracteristica=true";
        }

        adminService.registrarCaracteristica(nombre, padreId);
        return "redirect:/admin/panel";
    }

    @PostMapping("/caracteristica/eliminar/{id}")
    public String eliminarCaracteristica(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/login";
        }
        adminService.eliminarCaracteristica(id);
        return "redirect:/admin/panel";
    }

    // ==================== LOGOUT ====================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}