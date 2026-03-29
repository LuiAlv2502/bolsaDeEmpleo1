package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Administrador;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
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

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "No se ha encontrado un usuario o la cuenta no ha sido aprobada.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String validarLogin(@RequestParam("credencial") String credencial, @RequestParam("password") String password,Model model,
                               HttpSession session) {
        if(credencial == null || credencial.isEmpty() || password == null || password.isEmpty()){
            model.addAttribute("error", "Por favor, ingrese su usuario y contraseña.");
            return "login";
        }

        Administrador admin = adminService.login(credencial, password);
        if(admin != null){
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("adminNombre", admin.getNombre());
            session.setAttribute("tipoUsuario", "admin");
            return "redirect:/admin/panel";

        }
        Empresa empresa = empresaService.login(credencial, password);
        if(empresa != null){
            session.setAttribute("empresaId", empresa.getId());
            session.setAttribute("empresaNombre", empresa.getNombre());
            session.setAttribute("tipoUsuario", "empresa");
            return "redirect:/empresa/dashboard";
        }
        Oferente oferente = oferenteService.login(credencial, password);
        if(oferente != null){
            session.setAttribute("oferenteId", oferente.getIdentificacion());
            session.setAttribute("oferenteNombre", oferente.getNombre());
            session.setAttribute("tipoUsuario", "oferente");
            return "redirect:/oferente/dashboard";

        }
        model.addAttribute("error", "No se ha encontrado un usuario o la cuenta no ha sido aprobada.");
        return "login";

    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

}

