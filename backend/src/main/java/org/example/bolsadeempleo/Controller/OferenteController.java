package org.example.bolsadeempleo.Controller;

import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.logic.service.Busquedaservice;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.example.bolsadeempleo.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/oferente")
public class OferenteController {

    @Autowired private OferenteService oferenteService;
    @Autowired private CaracteristicaRepository caracteristicaRepository;
    @Autowired private Busquedaservice busquedaservice;

    @Value("${cv.upload.dir:uploads/cv}")
    private String cvUploadDir;

    // ── Registro (público) ────────────────────────────────────────────────────
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> body) {
        String identificacion    = body.get("identificacion");
        String nombre            = body.get("nombre");
        String correo            = body.get("correo");
        String password          = body.get("password");
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

        correo = correo.trim().toLowerCase();
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return ResponseEntity.badRequest().body(Map.of("error", "El correo no tiene un formato válido."));

        Oferente oferente = new Oferente();
        oferente.setIdentificacion(identificacion);
        oferente.setNombre(nombre);
        oferente.setCorreo(correo);
        oferente.setClave(password);
        oferente.setAprobado(false);

        if (!oferenteService.registrar(oferente))
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una cuenta con ese correo o identificación."));

        return ResponseEntity.ok(Map.of("mensaje", "Registro exitoso."));
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@AuthenticationPrincipal CustomUserDetails oferente) {
        return ResponseEntity.ok(Map.of(
                "nombre", oferente.getNombre(),
                "tieneCv", oferenteService.tieneCurriculum(oferente.getUserId())
        ));
    }

    // ── Búsqueda de puestos ───────────────────────────────────────────────────
    @GetMapping("/puestos/buscar")
    public ResponseEntity<?> buscarPuestos(
            @AuthenticationPrincipal CustomUserDetails oferente,
            @RequestParam(value = "tipo", defaultValue = "publica") String tipo,
            @RequestParam(value = "palabra", required = false) String palabra,
            @RequestParam(value = "salarioMin", required = false) BigDecimal salarioMin,
            @RequestParam(value = "caracteristica", required = false) String caracteristica) {

        Long caracteristicaId = (caracteristica != null && !caracteristica.isBlank())
                ? Long.valueOf(caracteristica) : null;

        boolean esPrivada = "privada".equalsIgnoreCase(tipo);
        boolean esTodos   = "todos".equalsIgnoreCase(tipo);

        if (esTodos) {
            return ResponseEntity.ok(Map.of(
                    "resultados", busquedaservice.buscarTodosLosPuestos(palabra, salarioMin, caracteristicaId),
                    "caracteristicas", caracteristicaRepository.findAll()
            ));
        } else if (esPrivada) {
            return ResponseEntity.ok(Map.of(
                    "resultados", busquedaservice.buscarPuestosPrivados(palabra, salarioMin, caracteristicaId),
                    "caracteristicas", caracteristicaRepository.findAll()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "resultados", busquedaservice.buscarPuestosPublicos(palabra, salarioMin, caracteristicaId),
                    "caracteristicas", caracteristicaRepository.findAll()
            ));
        }
    }

    // ── Perfil ────────────────────────────────────────────────────────────────
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(@AuthenticationPrincipal CustomUserDetails oferente) {
        return ResponseEntity.ok(oferenteService.obtenerPorIdentificacion(oferente.getUserId()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Oferente body,
                                               @AuthenticationPrincipal CustomUserDetails oferente) {
        body.setIdentificacion(oferente.getUserId());
        oferenteService.actualizarDatos(body);
        return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado correctamente."));
    }

    // ── Habilidades ───────────────────────────────────────────────────────────
    @GetMapping("/habilidades")
    public ResponseEntity<?> habilidades(@AuthenticationPrincipal CustomUserDetails oferente) {
        return ResponseEntity.ok(Map.of(
                "habilidades", oferenteService.getHabilidades(oferente.getUserId()),
                "caracteristicas", caracteristicaRepository.findAll()
        ));
    }

    @PostMapping("/habilidades/agregar")
    public ResponseEntity<?> agregarHabilidad(@AuthenticationPrincipal CustomUserDetails oferente,
                                               @RequestBody Map<String, Object> body) {
        Long caracteristicaId = Long.valueOf(body.get("caracteristicaId").toString());
        Integer nivel = Integer.parseInt(body.get("nivel").toString());
        if (nivel < 1 || nivel > 5)
            return ResponseEntity.badRequest().body(Map.of("error", "El nivel debe estar entre 1 y 5."));
        boolean ok = oferenteService.agregarOActualizarHabilidad(oferente.getUserId(), caracteristicaId, nivel);
        return ResponseEntity.ok(Map.of("mensaje", ok ? "Habilidad agregada." : "Habilidad actualizada."));
    }

    @DeleteMapping("/habilidades/{habilidadId}")
    public ResponseEntity<?> eliminarHabilidad(@PathVariable Long habilidadId,
                                                @AuthenticationPrincipal CustomUserDetails oferente) {
        oferenteService.eliminarHabilidad(oferente.getUserId(), habilidadId);
        return ResponseEntity.ok(Map.of("mensaje", "Habilidad eliminada."));
    }

    // ── CV ────────────────────────────────────────────────────────────────────
    @GetMapping("/cv")
    public ResponseEntity<?> estadoCV(@AuthenticationPrincipal CustomUserDetails oferente) {
        return ResponseEntity.ok(Map.of("tieneCv", oferenteService.tieneCurriculum(oferente.getUserId())));
    }

    @PostMapping("/cv/subir")
    public ResponseEntity<?> subirCV(@AuthenticationPrincipal CustomUserDetails oferente,
                                      @RequestParam("archivo") MultipartFile archivo) {
        try {
            boolean ok = oferenteService.subirCurriculum(oferente.getUserId(), archivo);
            if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "El archivo debe ser un PDF válido."));
            return ResponseEntity.ok(Map.of("mensaje", "CV subido correctamente."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al guardar el archivo."));
        }
    }

    /**
     * Ver CV: el propio oferente puede verlo; una empresa también puede verlo
     * (el SecurityConfig ya permite ROLE_EMPRESA y ROLE_OFERENTE para esta ruta).
     * Si es OFERENTE sólo puede ver su propio CV.
     */
    @GetMapping("/cv/ver/{identificacion}")
    public ResponseEntity<Resource> verCV(@PathVariable String identificacion,
                                           @AuthenticationPrincipal CustomUserDetails usuario) {
        // Si es oferente sólo puede ver su propio CV
        if (usuario.getRole().equals("ROLE_OFERENTE")
                && !identificacion.equals(usuario.getUserId())) {
            return ResponseEntity.status(403).build();
        }

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
