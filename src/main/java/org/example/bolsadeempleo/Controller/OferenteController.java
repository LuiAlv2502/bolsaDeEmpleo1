package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/oferente")
public class OferenteController {

    @Autowired
    private OferenteService oferenteService;

    // ==================== REGISTRO ====================

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("oferente", new Oferente());
        return "oferente/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @ModelAttribute("oferente") Oferente oferente,
            @RequestParam("password") String password,
            @RequestParam("confirmarPassword") String confirmarPassword,
            Model model) {

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "oferente/registro";
        }

        // Validar longitud mínima de contraseña
        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "oferente/registro";
        }

        // Validar campos obligatorios
        if (oferente.getIdentificacion() == null || oferente.getIdentificacion().isBlank()) {
            model.addAttribute("error", "La identificación es obligatoria.");
            return "oferente/registro";
        }

        if (oferente.getNombre() == null || oferente.getNombre().isBlank()) {
            model.addAttribute("error", "El nombre es obligatorio.");
            return "oferente/registro";
        }

        if (oferente.getCorreo() == null || oferente.getCorreo().isBlank()) {
            model.addAttribute("error", "El correo es obligatorio.");
            return "oferente/registro";
        }

        // Asignar la contraseña y estado inicial
        oferente.setClave(password);
        oferente.setAprobado(false);

        boolean registrado = oferenteService.registrar(oferente);

        if (!registrado) {
            model.addAttribute("error", "Ya existe una cuenta registrada con ese correo electrónico.");
            return "oferente/registro";
        }

        model.addAttribute("success", "Registro exitoso. Tu cuenta será revisada por un administrador antes de poder ingresar.");
        model.addAttribute("oferente", new Oferente());
        return "oferente/registro";
    }

    // ==================== LOGIN (redirige al login unificado) ====================

    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("oferenteId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("nombre", session.getAttribute("oferenteNombre"));
        return "oferente/dashboard";
    }

    // ==================== LOGOUT ====================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}