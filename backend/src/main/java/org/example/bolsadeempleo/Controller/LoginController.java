package org.example.bolsadeempleo.Controller;

import org.example.bolsadeempleo.logic.Administrador;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.security.CustomUserDetails;
import org.example.bolsadeempleo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired private AdminService adminService;
    @Autowired private EmpresaService empresaService;
    @Autowired private OferenteService oferenteService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> validarLogin(@RequestBody Map<String, String> body) {
        String credencial = body.get("credencial");
        String password   = body.get("password");

        if (credencial == null || credencial.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Por favor, ingrese su usuario y contraseña."));
        }

        // ── Admin ──────────────────────────────────────────────────────────
        Administrador admin = adminService.login(credencial, password);
        if (admin != null) {
            CustomUserDetails details = new CustomUserDetails(
                    admin.getIdentificacion(),
                    admin.getPassword() != null ? admin.getPassword() : "",
                    "ROLE_ADMIN",
                    String.valueOf(admin.getId()),
                    admin.getNombre()
            );
            String token = jwtUtil.generateToken(details);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tipo", "admin",
                    "nombre", admin.getNombre(),
                    "id", admin.getId()
            ));
        }

        // ── Empresa ────────────────────────────────────────────────────────
        Empresa empresa = empresaService.login(credencial, password);
        if (empresa != null) {
            CustomUserDetails details = new CustomUserDetails(
                    empresa.getCorreo(),
                    empresa.getClave(),
                    "ROLE_EMPRESA",
                    String.valueOf(empresa.getId()),
                    empresa.getNombre()
            );
            String token = jwtUtil.generateToken(details);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tipo", "empresa",
                    "nombre", empresa.getNombre(),
                    "id", empresa.getId()
            ));
        }

        // ── Oferente ───────────────────────────────────────────────────────
        Oferente oferente = oferenteService.login(credencial, password);
        if (oferente != null) {
            CustomUserDetails details = new CustomUserDetails(
                    oferente.getCorreo(),
                    oferente.getClave(),
                    "ROLE_OFERENTE",
                    oferente.getIdentificacion(),
                    oferente.getNombre()
            );
            String token = jwtUtil.generateToken(details);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tipo", "oferente",
                    "nombre", oferente.getNombre(),
                    "id", oferente.getIdentificacion()
            ));
        }

        return ResponseEntity.status(401)
                .body(Map.of("error", "No se ha encontrado un usuario o la cuenta no ha sido aprobada."));
    }

    /** Logout del lado del cliente: el frontend descarta el token. */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente."));
    }
}
