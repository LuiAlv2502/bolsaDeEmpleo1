package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empresa")
public class Empresacontroller {

    @Autowired
    private EmpresaService empresaService;


    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresa/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @ModelAttribute("empresa") Empresa empresa,
            @RequestParam("password") String password,
            @RequestParam("confirmarPassword") String confirmarPassword,
            Model model) {

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "empresa/registro";
        }

        // Validar longitud mínima de contraseña
        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "empresa/registro";
        }

        // Validar campos obligatorios
        if (empresa.getNombre() == null || empresa.getNombre().isBlank()) {
            model.addAttribute("error", "El nombre de la empresa es obligatorio.");
            return "empresa/registro";
        }

        if (empresa.getCorreo() == null || empresa.getCorreo().isBlank()) {
            model.addAttribute("error", "El correo es obligatorio.");
            return "empresa/registro";
        }

        // Asignar contraseña y estado inicial
        empresa.setClave(password);
        empresa.setAprobado(false);

        boolean registrada = empresaService.registrar(empresa);

        if (!registrada) {
            model.addAttribute("error", "Ya existe una cuenta registrada con ese correo electrónico.");
            return "empresa/registro";
        }

        model.addAttribute("success", "Registro exitoso. Tu empresa será revisada por un administrador antes de poder ingresar.");
        model.addAttribute("empresa", new Empresa());
        return "empresa/registro";
    }

    // ==================== LOGIN (redirige al login unificado) ====================

    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("empresaId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("nombre", session.getAttribute("empresaNombre"));
        return "empresa/dashboard";
    }

    // ==================== LOGOUT ====================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}