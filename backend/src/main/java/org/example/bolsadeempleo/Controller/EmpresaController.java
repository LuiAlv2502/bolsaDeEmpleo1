package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private OferenteService oferenteService;
    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    private Long getEmpresaId(HttpSession session) {
        Object id = session.getAttribute("empresaId");
        return id == null ? null : (Long) id;
    }

    private ResponseEntity<?> noAutorizado() {
        return ResponseEntity.status(401).body(Map.of("error", "No autorizado."));
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String correo = body.get("correo");
        String password = body.get("password");
        String confirmarPassword = body.get("confirmarPassword");

        if (nombre == null || nombre.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio."));
        if (correo == null || correo.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio."));

        // Normalización + validación de formato de correo
        correo = correo.trim().toLowerCase();
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo no tiene un formato válido."));
        }

        if (password == null || !password.equals(confirmarPassword))
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden."));
        if (password.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 8 caracteres."));

        Empresa empresa = new Empresa();
        empresa.setNombre(nombre);
        empresa.setCorreo(correo);
        empresa.setClave(password);
        empresa.setAprobado(false);

        if (!empresaService.registrar(empresa))
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una cuenta (empresa u oferente) con ese correo."));

        return ResponseEntity.ok(Map.of("mensaje", "Se logró registrar la empresa."));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();
        return ResponseEntity.ok(Map.of("nombre", session.getAttribute("empresaNombre")));
    }

    @GetMapping("/puestos")
    public ResponseEntity<?> puestos(HttpSession session) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();
        return ResponseEntity.ok(Map.of(
                "puestos", empresaService.getPuestosPorEmpresa(empresaId),
                "caracteristicas", caracteristicaRepository.findAll(),
                "nombre", session.getAttribute("empresaNombre")
        ));
    }

    @PostMapping("/publicarPuesto")
    public ResponseEntity<?> publicarPuesto(HttpSession session, @RequestBody Map<String, Object> body) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();

        String descripcion = (String) body.get("descripcion");
        BigDecimal salario = new BigDecimal(body.get("salario").toString());
        boolean publica = Boolean.parseBoolean(body.get("publica").toString());
        String moneda = (String) body.get("moneda");

        List<Long> caracteristicaIds = new ArrayList<>();
        List<Integer> nivelesValidos = new ArrayList<>();

        if (body.get("caracteristicaIds") instanceof List<?> ids) {
            List<?> niveles = body.get("niveles") instanceof List<?> n ? n : List.of();
            for (int i = 0; i < ids.size(); i++) {
                String idStr = ids.get(i) != null ? ids.get(i).toString() : "";
                if (!idStr.isBlank()) {
                    try {
                        caracteristicaIds.add(Long.parseLong(idStr));
                        nivelesValidos.add(i < niveles.size() && niveles.get(i) != null
                                ? Integer.parseInt(niveles.get(i).toString()) : 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        Puesto puesto = empresaService.publicarPuesto(empresaId, descripcion, salario, publica, moneda, caracteristicaIds, nivelesValidos);
        if (puesto == null)
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo publicar el puesto."));
        return ResponseEntity.ok(puesto);
    }

    @GetMapping("/puestos/{id}/detalle")
    public ResponseEntity<?> detallePuesto(@PathVariable Long id, HttpSession session) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();
        Puesto puesto = empresaService.getPuesto(id);
        if (puesto == null || !puesto.getEmpresa().getId().equals(empresaId))
            return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado."));
        return ResponseEntity.ok(Map.of(
                "puesto", puesto,
                "candidatos", empresaService.buscarCandidatos(id)
        ));
    }

    @PostMapping("/puestos/{id}/desactivar")
    public ResponseEntity<?> desactivarPuesto(@PathVariable Long id, HttpSession session) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();
        empresaService.desactivarPuesto(id);
        return ResponseEntity.ok(Map.of("mensaje", "Puesto desactivado."));
    }

    @GetMapping("/candidatos/buscar")
    public ResponseEntity<?> buscarCandidatos(@RequestParam("puestoId") Long puestoId, HttpSession session) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();
        Puesto puesto = empresaService.getPuesto(puestoId);
        return ResponseEntity.ok(Map.of(
                "puesto", puesto,
                "candidatos", empresaService.buscarCandidatos(puestoId)
        ));
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(HttpSession session) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return noAutorizado();
        return ResponseEntity.ok(empresaService.getById(empresaId));
    }
}
