package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ── Utilidad: verificar sesión ────────────────────────────────────────────

    private boolean esAdmin(HttpSession session) {
        return session.getAttribute("adminId") != null;
    }

    // ── LOGIN (redirige al unificado) ─────────────────────────────────────────

    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("adminNombre"));
        model.addAttribute("cantidadEmpresasPendientes",
                adminService.listarEmpresasPendientes().size());
        model.addAttribute("cantidadOferentesPendientes",
                adminService.listarOferentesPendientes().size());
        model.addAttribute("cantidadCaracteristicas",
                adminService.listarTodasCaracteristicas().size());
        return "admin/dashboard";
    }

    // ── PANEL (equivalente anterior, redirige al dashboard) ───────────────────

    @GetMapping("/panel")
    public String panel(HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        return "redirect:/admin/dashboard";
    }

    // ── EMPRESAS PENDIENTES ───────────────────────────────────────────────────

    @GetMapping("/empresas/pendientes")
    public String empresasPendientes(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("adminNombre"));
        model.addAttribute("empresasPendientes", adminService.listarEmpresasPendientes());
        return "admin/empresas-pendientes";
    }

    @PostMapping("/empresa/aprobar/{id}")
    public String aprobarEmpresa(@PathVariable Long id, HttpSession session,
                                 RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) return "redirect:/login";

        boolean ok = adminService.autorizarEmpresa(id);
        redirectAttrs.addFlashAttribute(ok ? "success" : "error",
                ok ? "Empresa aprobada." : "No se encontró la empresa.");
        return "redirect:/admin/empresas/pendientes";
    }

    // ── OFERENTES PENDIENTES ──────────────────────────────────────────────────

    @GetMapping("/oferentes/pendientes")
    public String oferentesPendientes(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("adminNombre"));
        model.addAttribute("oferentesPendientes", adminService.listarOferentesPendientes());
        return "admin/oferentes-pendientes";
    }

    @PostMapping("/oferente/aprobar/{identificacion}")
    public String aprobarOferente(@PathVariable String identificacion, HttpSession session,
                                  RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) return "redirect:/login";

        boolean ok = adminService.autorizarOferente(identificacion);
        redirectAttrs.addFlashAttribute(ok ? "success" : "error",
                ok ? "Oferente aprobado." : "No se encontró el oferente.");
        return "redirect:/admin/oferentes/pendientes";
    }

    // ── CARACTERÍSTICAS ───────────────────────────────────────────────────────

    @GetMapping("/caracteristicas")
    public String caracteristicas(HttpSession session, Model model,
                                  @RequestParam(value = "parentId", required = false) Long parentId) {
        if (!esAdmin(session)) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("adminNombre"));

        if (parentId != null) {
            // Navegar dentro de una categoría
            model.addAttribute("categoriaActual",
                    adminService.obtenerCaracteristica(parentId).orElse(null));
            model.addAttribute("caracteristicas",
                    adminService.listarTodasCaracteristicas().stream()
                            .filter(c -> c.getParent() != null && c.getParent().getId().equals(parentId))
                            .toList());
        } else {
            // Mostrar raíces
            model.addAttribute("categoriaActual", null);
            model.addAttribute("caracteristicas", adminService.listarCaracteristicasRaiz());
        }

        model.addAttribute("todasCaracteristicas", adminService.listarTodasCaracteristicas());
        model.addAttribute("parentId", parentId);
        return "admin/caracteristicas";
    }

    @PostMapping("/caracteristica/nueva")
    public String crearCaracteristica(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "padreId", required = false) Long padreId,
            HttpSession session, RedirectAttributes redirectAttrs) {

        if (!esAdmin(session)) return "redirect:/login";

        if (nombre == null || nombre.isBlank()) {
            redirectAttrs.addFlashAttribute("error", "El nombre no puede estar vacío.");
        } else {
            adminService.registrarCaracteristica(nombre, padreId);
            redirectAttrs.addFlashAttribute("success", "Característica creada.");
        }

        String redirect = "/admin/caracteristicas";
        if (padreId != null) redirect += "?parentId=" + padreId;
        return "redirect:" + redirect;
    }

    @PostMapping("/caracteristica/eliminar/{id}")
    public String eliminarCaracteristica(@PathVariable Long id, HttpSession session,
                                         RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) return "redirect:/login";

        // Recordar el padre antes de eliminar para redirigir bien
        Long parentId = adminService.obtenerCaracteristica(id)
                .map(c -> c.getParent() != null ? c.getParent().getId() : null)
                .orElse(null);

        adminService.eliminarCaracteristica(id);
        redirectAttrs.addFlashAttribute("success", "Característica eliminada.");

        String redirect = "/admin/caracteristicas";
        if (parentId != null) redirect += "?parentId=" + parentId;
        return "redirect:" + redirect;
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}