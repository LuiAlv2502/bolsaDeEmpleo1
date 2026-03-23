package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.logic.PuestoCaracteristica;
import org.example.bolsadeempleo.logic.service.EmpresaService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/empresa")
public class Empresacontroller {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private OferenteService oferenteService;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    // ── Utilidad: verificar sesión ────────────────────────────────────────────

    private Long getEmpresaId(HttpSession session) {
        Object id = session.getAttribute("empresaId");
        if (id == null) return null;
        return (Long) id;
    }

    // ── REGISTRO ──────────────────────────────────────────────────────────────

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

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "empresa/registro";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "empresa/registro";
        }
        if (empresa.getNombre() == null || empresa.getNombre().isBlank()) {
            model.addAttribute("error", "El nombre de la empresa es obligatorio.");
            return "empresa/registro";
        }
        if (empresa.getCorreo() == null || empresa.getCorreo().isBlank()) {
            model.addAttribute("error", "El correo es obligatorio.");
            return "empresa/registro";
        }

        empresa.setClave(password);
        empresa.setAprobado(false);

        if (!empresaService.registrar(empresa)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo electrónico.");
            return "empresa/registro";
        }

        model.addAttribute("success", "Registro exitoso. Tu empresa será revisada por un administrador.");
        model.addAttribute("empresa", new Empresa());
        return "empresa/registro";
    }

    // ── LOGIN (redirige al unificado) ─────────────────────────────────────────

    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("empresaNombre"));
        return "empresa/dashboard";
    }

    // ── MIS PUESTOS ───────────────────────────────────────────────────────────

    @GetMapping("/puestos")
    public String misPuestos(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        model.addAttribute("puestos", empresaService.listarPuestosPorEmpresa(empresaId));
        return "empresa/mis-puestos";
    }

    // ── PUBLICAR PUESTO ───────────────────────────────────────────────────────

    @GetMapping("/puestos/nuevo")
    public String mostrarFormularioPuesto(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        return "empresa/publicar-puesto";
    }

    @PostMapping("/puestos/nuevo")
    public String publicarPuesto(
            HttpSession session,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("salario") BigDecimal salario,
            @RequestParam("publica") boolean publica,
            @RequestParam(value = "caracteristicaIds", required = false) List<Long> caracteristicaIds,
            @RequestParam(value = "niveles", required = false) List<Integer> niveles,
            RedirectAttributes redirectAttrs) {

        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        if (caracteristicaIds == null) caracteristicaIds = List.of();
        if (niveles == null) niveles = List.of();

        Puesto puesto = empresaService.publicarPuesto(
                empresaId, descripcion, salario, publica, caracteristicaIds, niveles);

        if (puesto == null) {
            redirectAttrs.addFlashAttribute("error", "No se pudo publicar el puesto.");
            return "redirect:/empresa/puestos/nuevo";
        }

        redirectAttrs.addFlashAttribute("success", "Puesto publicado exitosamente.");
        return "redirect:/empresa/puestos";
    }

    // ── DESACTIVAR PUESTO ─────────────────────────────────────────────────────

    @PostMapping("/puestos/{id}/desactivar")
    public String desactivarPuesto(@PathVariable Long id, HttpSession session,
                                   RedirectAttributes redirectAttrs) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        empresaService.desactivarPuesto(id, empresaId);
        redirectAttrs.addFlashAttribute("success", "Puesto desactivado.");
        return "redirect:/empresa/puestos";
    }

    // ── BUSCAR CANDIDATOS ─────────────────────────────────────────────────────

    @GetMapping("/candidatos/buscar")
    public String buscarCandidatos(@RequestParam("puestoId") Long puestoId,
                                   HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        Puesto puesto = empresaService.obtenerPuesto(puestoId);
        if (puesto == null || !puesto.getEmpresa().getId().equals(empresaId)) {
            return "redirect:/empresa/puestos";
        }

        model.addAttribute("puesto", puesto);
        model.addAttribute("candidatos", empresaService.buscarCandidatos(puestoId));
        return "empresa/buscar-candidatos";
    }

    // ── VER DETALLE CANDIDATO ─────────────────────────────────────────────────

    @GetMapping("/candidatos/{identificacion}")
    public String verCandidato(@PathVariable String identificacion,
                               HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        Oferente oferente = empresaService.verDetalleCandidato(identificacion);
        if (oferente == null) return "redirect:/empresa/puestos";

        model.addAttribute("oferente", oferente);
        model.addAttribute("habilidades", oferenteService.listarHabilidades(identificacion));
        return "empresa/ver-candidato";
    }

    // ── PERFIL EMPRESA ────────────────────────────────────────────────────────

    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        model.addAttribute("empresa", empresaService.obtenerPorId(empresaId));
        return "empresa/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("empresa") Empresa empresa,
                                   HttpSession session, RedirectAttributes redirectAttrs) {
        Long empresaId = getEmpresaId(session);
        if (empresaId == null) return "redirect:/login";

        empresa.setId(empresaId);
        empresaService.actualizarDatos(empresa);

        // Actualizar nombre en sesión
        session.setAttribute("empresaNombre", empresa.getNombre());
        redirectAttrs.addFlashAttribute("success", "Datos actualizados correctamente.");
        return "redirect:/empresa/perfil";
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}