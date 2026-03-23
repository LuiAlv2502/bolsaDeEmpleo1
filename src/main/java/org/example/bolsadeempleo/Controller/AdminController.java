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


    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        return "redirect:/admin/panel";
    }


    @GetMapping("/panel")
    public String panel(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("adminNombre"));
        model.addAttribute("empresasPendientes", adminService.listarEmpresasPendientes());
        model.addAttribute("oferentesPendientes", adminService.listarOferentesPendientes());
        model.addAttribute("caracteristicas", adminService.listarTodasCaracteristicas());
        return "admin/panel";
    }

    // ── EMPRESAS PENDIENTES ───────────────────────────────────────────────────

    @GetMapping("/empresas/pendientes")
    public String empresasPendientes(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";
        return "redirect:/admin/panel";
    }

    @PostMapping("/empresa/aprobar/{id}")
    public String aprobarEmpresa(@PathVariable Long id, HttpSession session,
                                 RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) return "redirect:/login";

        boolean ok = adminService.autorizarEmpresa(id);
        redirectAttrs.addFlashAttribute(ok ? "success" : "error",
                ok ? "Empresa aprobada." : "No se encontró la empresa.");
        return "redirect:/admin/panel";
    }

    // ── OFERENTES PENDIENTES ──────────────────────────────────────────────────

    @GetMapping("/oferentes/pendientes")
    public String oferentesPendientes(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";
        return "redirect:/admin/panel";
    }

    @PostMapping("/oferente/aprobar/{identificacion}")
    public String aprobarOferente(@PathVariable String identificacion, HttpSession session,
                                  RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) return "redirect:/login";

        boolean ok = adminService.autorizarOferente(identificacion);
        redirectAttrs.addFlashAttribute(ok ? "success" : "error",
                ok ? "Oferente aprobado." : "No se encontró el oferente.");
        return "redirect:/admin/panel";
    }


    @GetMapping("/caracteristicas")
    public String caracteristicas(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";
        return "redirect:/admin/panel";
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
        return "redirect:/admin/panel";
    }

    @PostMapping("/caracteristica/eliminar/{id}")
    public String eliminarCaracteristica(@PathVariable Long id, HttpSession session,
                                         RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) return "redirect:/login";

        adminService.eliminarCaracteristica(id);
        redirectAttrs.addFlashAttribute("success", "Característica eliminada.");
        return "redirect:/admin/panel";
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}