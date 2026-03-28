package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Administrador;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private OferenteService oferenteService;

    // ==================== LOGIN UNIFICADO (auto-detección) ====================

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas o cuenta no aprobada.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam("credencial") String credencial,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        // Validar campos vacíos
        if (credencial == null || credencial.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Por favor ingresa tus credenciales y contraseña.");
            return "login";
        }

        // 1. Intentar como Administrador (identificacion + password)
        Administrador admin = adminService.login(credencial, password);
        if (admin != null) {
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("adminNombre", admin.getNombre());
            session.setAttribute("tipoUsuario", "admin");
            return "redirect:/admin/panel";
        }

        // 2. Intentar como Empresa (correo + clave)
        Empresa empresa = empresaService.login(credencial, password);
        if (empresa != null) {
            session.setAttribute("empresaId", empresa.getId());
            session.setAttribute("empresaNombre", empresa.getNombre());
            session.setAttribute("tipoUsuario", "empresa");
            return "redirect:/empresa/dashboard";
        }

        // 3. Intentar como Oferente (correo + clave)
        Oferente oferente = oferenteService.login(credencial, password);
        if (oferente != null) {
            session.setAttribute("oferenteId", oferente.getIdentificacion());
            session.setAttribute("oferenteNombre", oferente.getNombre());
            session.setAttribute("tipoUsuario", "oferente");
            return "redirect:/oferente/dashboard";
        }

        // Ninguno coincidió
        model.addAttribute("error", "Credenciales incorrectas o cuenta aún no aprobada.");
        return "login";
    }

    // ==================== LOGOUT GLOBAL ====================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}

