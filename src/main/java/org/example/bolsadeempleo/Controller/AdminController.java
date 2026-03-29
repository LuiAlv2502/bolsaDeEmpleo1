package org.example.bolsadeempleo.Controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.example.bolsadeempleo.logic.service.OferenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
//listo
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    private OferenteService oferenteService;

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
        model.addAttribute("empresasPendientes", adminService.getEmpresasPendientes());
        model.addAttribute("oferentesPendientes", adminService.getOferentesPendientes());
        model.addAttribute("puestos", adminService.todosLosPuestos());

        if (actualId != null) {
            var actual = adminService.obtenerCaracteristica(actualId);
            actual.ifPresent(c -> {
                model.addAttribute("actual", c);
                model.addAttribute("caracteristicas", adminService.listarHijos(actualId));
                model.addAttribute("ruta", adminService.getRuta(actualId));
            });
        } else {
            model.addAttribute("caracteristicas", adminService.getCaracteristicasRaiz());
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
    @GetMapping("/reporte/puestos")
    public ResponseEntity<byte[]> reportePuestos(@RequestParam int mes, @RequestParam int anio,
                                                         HttpSession session){
        List<Puesto> puestos = adminService.puestosPorMes(mes, anio);

        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            PdfWriter writer = new  PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Reporte de Puestos - " + meses[mes] + " " + anio).setBold().setFontSize(16));
            doc.add(new Paragraph(" "));

            if(puestos.isEmpty()) {
                doc.add(new Paragraph("No se encontraron puestos publicados en este mes."));
            }else {
                Table tabla = new Table(UnitValue.createPercentArray(new float[]{30, 20, 25, 25})).useAllAvailableWidth();
                tabla.addHeaderCell(new Cell().add(new Paragraph("Nombre del Puesto").setBold()));
                tabla.addHeaderCell(new Cell().add(new Paragraph("Salario").setBold()));
                tabla.addHeaderCell(new Cell().add(new Paragraph("Empresa").setBold()));
                tabla.addHeaderCell(new Cell().add(new Paragraph("Fecha de Creacion").setBold()));


                for (Puesto puesto : puestos) {
                    tabla.addCell(new Cell().add(new Paragraph(
                            puesto.getDescripcion() != null ? puesto.getDescripcion() : "—")));

                    String salario = (puesto.getMoneda() != null && puesto.getMoneda().equals("CRC") ? "CRC " : "USD ")
                            + (puesto.getSalario() != null ? puesto.getSalario().toPlainString() : "0");
                    tabla.addCell(new Cell().add(new Paragraph(salario)));

                    tabla.addCell(new Cell().add(new Paragraph(
                            puesto.getEmpresa() != null ? puesto.getEmpresa().getNombre() : "—")));

                    tabla.addCell(new Cell().add(new Paragraph(
                            puesto.getFechaPublicacion() != null ? formatter.format(puesto.getFechaPublicacion()) : "—")));

                }
                doc.add(tabla);
            }
            doc.close();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        byte[] contenido = out.toByteArray();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_puestos_" + anio + "_" + mes + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).contentLength(contenido.length).body(contenido);

    }




}
