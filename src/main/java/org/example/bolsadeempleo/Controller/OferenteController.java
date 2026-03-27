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

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("oferente", new Oferente());
        return "oferente/registro";
    }

    @PostMapping("/registro")
    public String registro(@ModelAttribute("oferente") Oferente oferente,
                           @RequestParam("password") String password,
                           @RequestParam("confirmarPassword") String  confirmarPassword,
                           Model model) {
        if(!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "oferente/registro";
        }
        if (password.length() < 8){
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "oferente/registro";
        }
        if (oferente.getIdentificacion() == null || oferente.getIdentificacion().isEmpty()){
            model.addAttribute("error", "La identificación es obligatoria.");
            return "oferente/registro";
        }
        if(oferente.getNombre() == null || oferente.getNombre().isEmpty()){
            model.addAttribute("error", "La nombre es obligatoria.");
            return "oferente/registro";
        }
        if(oferente.getCorreo() == null || oferente.getCorreo().isEmpty()){
            model.addAttribute("error", "La correo es obligatoria.");
            return "oferente/registro";
        }
        oferente.setClave(password);
        oferente.setAprobado(false);

        if(!oferenteService.registrar(oferente)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo o identificación.");
            return "oferente/registro";
        }
        model.addAttribute("success", "Registro exitoso");
        model.addAttribute("oferente", new Oferente());
        return "oferente/registro";
    }
    @GetMapping("/login")
    public String login() {
        return "redirect:/login";
    }

    private String getOferenteId(HttpSession session) {
        Object id = session.getAttribute("oferenteId");
        if (id == null) {
            return null;
        }
        return (String) id;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String id = getOferenteId(session);
        model.addAttribute("nombre", session.getAttribute("oferenteNombre"));
        model.addAttribute("tieneCv", oferenteService.tieneCurriculum(id));
        return "oferente/dashboard";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        String id = getOferenteId(session);
        model.addAttribute("oferente", oferenteService.obtenerPorIdentificacion(id));
        return "oferente/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("oferente") Oferente oferente,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        String id = getOferenteId(session);
        oferente.setIdentificacion(id);
        oferenteService.actualizarDatos(oferente);

        session.setAttribute("oferenteNombre", oferente.getNombre());
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        return "redirect:/oferente/perfil";
    }

    @GetMapping("/habilidades")
    public String habilidades(HttpSession session, Model model) {
        String id = getOferenteId(session);
        model.addAttribute("habilidades", oferenteService.listarHabilidades(id));
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        return "oferente/habilidades";
    }

    @PostMapping("/habilidades/agregar")
    public String agregarHabilidad(HttpSession session, Model model, @RequestParam("caracteristicaId") Long caracteristicaId,
            @RequestParam("nivel") Integer nivel
            ,RedirectAttributes redirectAttributes) {
        String id = getOferenteId(session);
        if(nivel < 1 || nivel > 5) {
            redirectAttributes.addFlashAttribute("error", "El nivel debe estar entre 1 y 5.");
            return "redirect:/oferente/habilidades";
        }
        boolean aprobar = oferenteService.agregarOActualizarHabilidad(id, caracteristicaId, nivel);
        if(!aprobar){
            redirectAttributes.addFlashAttribute("error", "Habilidad agregada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Habilidad agregada.");
        }
        return  "redirect:/oferente/habilidades";

    }
    @PostMapping("/habilidades/{habilidadId}/eliminar")
    public String eliminarHabilidad(@PathVariable Long habilidadId, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String id = getOferenteId(session);
        oferenteService.eliminarHabilidad(habilidadId, id);
        redirectAttributes.addFlashAttribute("success", "Habilidad eliminada.");
        return "redirect:/oferente/habilidades";
    }
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

    @GetMapping("/cv/ver/{identificacion}")
    public ResponseEntity<Resource> verCV(@PathVariable String identificacion,
                                          HttpSession session) {
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


}
