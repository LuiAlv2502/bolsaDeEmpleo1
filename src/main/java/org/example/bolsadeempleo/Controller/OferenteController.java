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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/oferente")
public class OferenteController {

    @Autowired
    private OferenteService oferenteService;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Value("${cv.upload.dir:uploads/cv}")
    private String cvUploadDir;

    // ── Utilidad: identificación desde sesión ─────────────────────────────────

    private String getOferenteId(HttpSession session) {
        Object id = session.getAttribute("oferenteId");
        return id != null ? (String) id : null;
    }

    // ── REGISTRO ──────────────────────────────────────────────────────────────

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

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "oferente/registro";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "oferente/registro";
        }
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

        oferente.setClave(password);
        oferente.setAprobado(false);

        if (!oferenteService.registrar(oferente)) {
            model.addAttribute("error", "Ya existe una cuenta con esa identificación o correo electrónico.");
            return "oferente/registro";
        }

        model.addAttribute("success", "Registro exitoso. Tu cuenta será revisada por un administrador.");
        model.addAttribute("oferente", new Oferente());
        return "oferente/registro";
    }

    // ── LOGIN (redirige al unificado) ─────────────────────────────────────────

    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("oferenteNombre"));
        model.addAttribute("tieneCv", oferenteService.tieneCurriculum(id));
        return "oferente/dashboard";
    }

    // ── PERFIL ────────────────────────────────────────────────────────────────

    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        model.addAttribute("oferente", oferenteService.obtenerPorIdentificacion(id));
        return "oferente/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("oferente") Oferente oferente,
                                   HttpSession session, RedirectAttributes redirectAttrs) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        oferente.setIdentificacion(id);
        oferenteService.actualizarDatos(oferente);

        session.setAttribute("oferenteNombre", oferente.getNombre());
        redirectAttrs.addFlashAttribute("success", "Datos actualizados correctamente.");
        return "redirect:/oferente/perfil";
    }

    // ── HABILIDADES ───────────────────────────────────────────────────────────

    @GetMapping("/habilidades")
    public String misHabilidades(HttpSession session, Model model) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        model.addAttribute("habilidades", oferenteService.listarHabilidades(id));
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        return "oferente/habilidades";
    }

    @PostMapping("/habilidades/agregar")
    public String agregarHabilidad(
            HttpSession session,
            @RequestParam("caracteristicaId") Long caracteristicaId,
            @RequestParam("nivel") Integer nivel,
            RedirectAttributes redirectAttrs) {

        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        if (nivel < 1 || nivel > 5) {
            redirectAttrs.addFlashAttribute("error", "El nivel debe estar entre 1 y 5.");
            return "redirect:/oferente/habilidades";
        }

        boolean ok = oferenteService.agregarOActualizarHabilidad(id, caracteristicaId, nivel);
        if (!ok) {
            redirectAttrs.addFlashAttribute("error", "No se pudo agregar la habilidad.");
        } else {
            redirectAttrs.addFlashAttribute("success", "Habilidad guardada.");
        }
        return "redirect:/oferente/habilidades";
    }

    @PostMapping("/habilidades/{habilidadId}/eliminar")
    public String eliminarHabilidad(@PathVariable Long habilidadId,
                                    HttpSession session, RedirectAttributes redirectAttrs) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        oferenteService.eliminarHabilidad(habilidadId, id);
        redirectAttrs.addFlashAttribute("success", "Habilidad eliminada.");
        return "redirect:/oferente/habilidades";
    }

    // ── SUBIR CV ──────────────────────────────────────────────────────────────

    @GetMapping("/cv")
    public String paginaCV(HttpSession session, Model model) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        model.addAttribute("tieneCv", oferenteService.tieneCurriculum(id));
        return "oferente/subir-cv";
    }

    @PostMapping("/cv/subir")
    public String subirCV(HttpSession session,
                          @RequestParam("archivo") MultipartFile archivo,
                          RedirectAttributes redirectAttrs) {
        String id = getOferenteId(session);
        if (id == null) return "redirect:/login";

        try {
            boolean ok = oferenteService.subirCurriculum(id, archivo);
            if (!ok) {
                redirectAttrs.addFlashAttribute("error", "El archivo debe ser un PDF válido.");
            } else {
                redirectAttrs.addFlashAttribute("success", "CV subido correctamente.");
            }
        } catch (IOException e) {
            redirectAttrs.addFlashAttribute("error", "Error al guardar el archivo. Intenta de nuevo.");
        }

        return "redirect:/oferente/cv";
    }

    // ── DESCARGAR / VER CV (para empresas también) ────────────────────────────

    @GetMapping("/cv/ver/{identificacion}")
    public ResponseEntity<Resource> verCV(@PathVariable String identificacion,
                                          HttpSession session) {
        // Solo empresas o el propio oferente pueden ver el CV
        Object tipoUsuario = session.getAttribute("tipoUsuario");
        if (tipoUsuario == null) return ResponseEntity.status(401).build();

        if (tipoUsuario.equals("oferente") && !identificacion.equals(session.getAttribute("oferenteId"))) {
            return ResponseEntity.status(403).build();
        }

        Oferente oferente = oferenteService.obtenerPorIdentificacion(identificacion);
        if (oferente == null || oferente.getCvPdf() == null || oferente.getCvPdf().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path archivo = Paths.get(cvUploadDir).resolve(oferente.getCvPdf());
            Resource resource = new UrlResource(archivo.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"cv_" + identificacion + ".pdf\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}