package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/oferente")
public class OferenteController {

    @Autowired
    private OferenteService oferenteService;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Value("${cv.upload.dir:uploads/cv}")
    private String cvUploadDir;

    private String getOferenteId(HttpSession session) {
        Object id = session.getAttribute("oferenteId");
        return id == null ? null : (String) id;
    }

    private ResponseEntity<?> noAutorizado() {
        return ResponseEntity.status(401).body(Map.of("error", "No autorizado."));
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> body) {
        String identificacion = body.get("identificacion");
        String nombre = body.get("nombre");
        String correo = body.get("correo");
        String password = body.get("password");
        String confirmarPassword = body.get("confirmarPassword");

        if (!password.equals(confirmarPassword))
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden."));
        if (password.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 8 caracteres."));
        if (identificacion == null || identificacion.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "La identificación es obligatoria."));
        if (nombre == null || nombre.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio."));
        if (correo == null || correo.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio."));

        // Normalización + validación de formato de correo
        correo = correo.trim().toLowerCase();
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo no tiene un formato válido."));
        }

        Oferente oferente = new Oferente();
        oferente.setIdentificacion(identificacion);
        oferente.setNombre(nombre);
        oferente.setCorreo(correo);
        oferente.setClave(password);
        oferente.setAprobado(false);

        if (!oferenteService.registrar(oferente))
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una cuenta (empresa u oferente) con ese correo o identificación."));

        return ResponseEntity.ok(Map.of("mensaje", "Registro exitoso."));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        return ResponseEntity.ok(Map.of(
                "nombre", session.getAttribute("oferenteNombre"),
                "tieneCv", oferenteService.tieneCurriculum(id)
        ));
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(HttpSession session) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        return ResponseEntity.ok(oferenteService.obtenerPorIdentificacion(id));
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Oferente oferente, HttpSession session) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        oferente.setIdentificacion(id);
        oferenteService.actualizarDatos(oferente);
        session.setAttribute("oferenteNombre", oferente.getNombre());
        return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado correctamente."));
    }

    @GetMapping("/habilidades")
    public ResponseEntity<?> habilidades(HttpSession session) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        return ResponseEntity.ok(Map.of(
                "habilidades", oferenteService.getHabilidades(id),
                "caracteristicas", caracteristicaRepository.findAll()
        ));
    }

    @PostMapping("/habilidades/agregar")
    public ResponseEntity<?> agregarHabilidad(HttpSession session, @RequestBody Map<String, Object> body) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        Long caracteristicaId = Long.valueOf(body.get("caracteristicaId").toString());
        Integer nivel = Integer.parseInt(body.get("nivel").toString());
        if (nivel < 1 || nivel > 5)
            return ResponseEntity.badRequest().body(Map.of("error", "El nivel debe estar entre 1 y 5."));
        boolean ok = oferenteService.agregarOActualizarHabilidad(id, caracteristicaId, nivel);
        return ResponseEntity.ok(Map.of("mensaje", ok ? "Habilidad agregada." : "Habilidad actualizada."));
    }

    @DeleteMapping("/habilidades/{habilidadId}")
    public ResponseEntity<?> eliminarHabilidad(@PathVariable Long habilidadId, HttpSession session) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        oferenteService.eliminarHabilidad(id, habilidadId);
        return ResponseEntity.ok(Map.of("mensaje", "Habilidad eliminada."));
    }

    @GetMapping("/cv")
    public ResponseEntity<?> estadoCV(HttpSession session) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        return ResponseEntity.ok(Map.of("tieneCv", oferenteService.tieneCurriculum(id)));
    }

    @PostMapping("/cv/subir")
    public ResponseEntity<?> subirCV(HttpSession session, @RequestParam("archivo") MultipartFile archivo) {
        String id = getOferenteId(session);
        if (id == null) return noAutorizado();
        try {
            boolean ok = oferenteService.subirCurriculum(id, archivo);
            if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "El archivo debe ser un PDF válido."));
            return ResponseEntity.ok(Map.of("mensaje", "CV subido correctamente."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al guardar el archivo."));
        }
    }

    @GetMapping("/cv/ver/{identificacion}")
    public ResponseEntity<Resource> verCV(@PathVariable String identificacion, HttpSession session) {
        Object tipoUsuario = session.getAttribute("tipoUsuario");
        if (tipoUsuario == null) return ResponseEntity.status(401).build();
        if (tipoUsuario.equals("oferente") && !identificacion.equals(session.getAttribute("oferenteId")))
            return ResponseEntity.status(403).build();

        Oferente oferente = oferenteService.obtenerPorIdentificacion(identificacion);
        if (oferente == null || oferente.getCvPdf() == null || oferente.getCvPdf().isBlank())
            return ResponseEntity.notFound().build();

        try {
            Path archivo = Paths.get(cvUploadDir).resolve(oferente.getCvPdf());
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists() || !resource.isReadable())
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cv_" + identificacion + ".pdf\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
