package org.example.bolsadeempleo.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
//listo
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    private boolean esAdmin(HttpSession session){
        return session.getAttribute("adminId") != null;
    }
    @GetMapping("/login")
    public String mostrarLogin() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session){
        if(!esAdmin(session)){
            return "redirect:/login";
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/panel")
    public String panel(HttpSession session, Model model, @RequestParam(value = "actualId", required = false) Long actualId) {
        if (!esAdmin(session)) return "redirect:/login";

        model.addAttribute("nombre", session.getAttribute("adminNombre"));
        model.addAttribute("empresasPendientes", adminService.listarEmpresasPendientes());
        model.addAttribute("oferentesPendientes", adminService.listarOferentesPendientes());
        model.addAttribute("puestos", adminService.listarTodosPuestos());

        if (actualId != null) {
            var actual = adminService.obtenerCaracteristica(actualId);
            actual.ifPresent(c -> {
                model.addAttribute("actual", c);
                model.addAttribute("caracteristicas", adminService.listarHijos(actualId));
                model.addAttribute("ruta", adminService.obtenerRuta(actualId));
            });
        } else {
            model.addAttribute("caracteristicas", adminService.listarCaracteristicasRaiz());
            model.addAttribute("ruta", List.of());
        }
        model.addAttribute("todasCaracteristicas", adminService.listarTodasCaracteristicas());
        return "admin/panel";
    }
        @GetMapping("/empresas/pendientes")
        public String empresasPendientes(HttpSession session, Model model){
            if(!esAdmin(session)){
                return "redirect:/login";
            }
            return "admin/panel";
        }

        @PostMapping("/empresa/aprobar/{id}")
        public String aprobarEmpresa(@PathVariable Long id, HttpSession session,RedirectAttributes redirectAttrs){

            if(!esAdmin(session)){
                return "redirect:/login";
            }

            boolean aprobar = adminService.autorizarEmpresa(id);
            redirectAttrs.addFlashAttribute(aprobar ? "success" : "error",
                    aprobar ? "Empresa aprobada." : "No se encontró la empresa.");
            return "redirect:/admin/panel";

        }

//        @GetMapping("/oferentes/pendietes")
//        public String oferentePendientes(HttpSession session, Model model){
//            if(!esAdmin(session)){
//                return "redirect:/login";
//            }
//            return "redirect:admin/panel";
//        }

        @PostMapping("/oferente/aprobar/{identificacion}")
        public String aprobarOferente(@PathVariable String identificacion, HttpSession session
                ,RedirectAttributes redirectAttrs) {

            if(!esAdmin(session)){
                return "redirect:/login";
            }

            boolean ok = adminService.autorizarOferente(identificacion);
            redirectAttrs.addFlashAttribute(ok ? "success" : "error",
                    ok ? "Oferente aprobado." : "No se encontró el oferente.");
            return "redirect:/admin/panel";
        }

        @PostMapping("/caracteristica/nueva")
        public String crearCaracteristica(@RequestParam String nombre, @RequestParam (value = "padreId", required = false) Long padreId,
                                          HttpSession session, RedirectAttributes redirectAttrs) {

            if(nombre == null || nombre.isBlank()){
                redirectAttrs.addFlashAttribute("error", "El nombre no puede estar vacio.");
            }else {
                adminService.registrarCaracteristica(nombre, padreId);
                redirectAttrs.addFlashAttribute("success", "Característica creada.");
        }
            return "redirect:/admin/panel";
            }
            @PostMapping("/caracteristica/eliminar/{id}")
            public String eliminarCaracteristica(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttrs) {
                if (!esAdmin(session)) {
                    return "redirect:/login";
                }

                boolean eliminado = adminService.eliminarCaracteristica(id);
                redirectAttrs.addFlashAttribute(eliminado ? "success" : "error",
                        eliminado ? "Característica eliminada." : "No se puede eliminar ya que es padre de otras caracteristicas");
                return "redirect:/admin/panel";


            }

            @GetMapping("/logout")
            public String logout(HttpSession session) {
                session.invalidate();
                return "redirect:/login";



    }




}
