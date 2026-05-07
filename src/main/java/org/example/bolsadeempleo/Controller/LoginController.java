package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Administrador;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private OferenteService oferenteService;

    @PostMapping("/login")
    public ResponseEntity<?> validarLogin(@RequestBody Map<String, String> body, HttpSession session) {
        String credencial = body.get("credencial");
        String password = body.get("password");

        if (credencial == null || credencial.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Por favor, ingrese su usuario y contraseña."));
        }

        Administrador admin = adminService.login(credencial, password);
        if (admin != null) {
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("adminNombre", admin.getNombre());
            session.setAttribute("tipoUsuario", "admin");
            return ResponseEntity.ok(Map.of("tipo", "admin", "nombre", admin.getNombre(), "id", admin.getId()));
        }

        Empresa empresa = empresaService.login(credencial, password);
        if (empresa != null) {
            session.setAttribute("empresaId", empresa.getId());
            session.setAttribute("empresaNombre", empresa.getNombre());
            session.setAttribute("tipoUsuario", "empresa");
            return ResponseEntity.ok(Map.of("tipo", "empresa", "nombre", empresa.getNombre(), "id", empresa.getId()));
        }

        Oferente oferente = oferenteService.login(credencial, password);
        if (oferente != null) {
            session.setAttribute("oferenteId", oferente.getIdentificacion());
            session.setAttribute("oferenteNombre", oferente.getNombre());
            session.setAttribute("tipoUsuario", "oferente");
            return ResponseEntity.ok(Map.of("tipo", "oferente", "nombre", oferente.getNombre(), "id", oferente.getIdentificacion()));
        }

        return ResponseEntity.status(401).body(Map.of("error", "No se ha encontrado un usuario o la cuenta no ha sido aprobada."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente."));
    }
}
