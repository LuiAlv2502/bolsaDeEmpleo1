package org.example.bolsadeempleo.Controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpSession;
import org.example.bolsadeempleo.logic.Caracteristica;
import org.example.bolsadeempleo.logic.Puesto;
import org.example.bolsadeempleo.logic.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    private boolean esAdmin(HttpSession session) {
        return session.getAttribute("adminId") != null;
    }

    private ResponseEntity<?> noAutorizado() {
        return ResponseEntity.status(401).body(Map.of("error", "No autorizado."));
    }

    @GetMapping("/panel")
    public ResponseEntity<?> panel(HttpSession session,
                                   @RequestParam(value = "actualId", required = false) Long actualId) {
        if (!esAdmin(session)) return noAutorizado();

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("nombre", session.getAttribute("adminNombre"));
        data.put("empresasPendientes", adminService.getEmpresasPendientes());
        data.put("oferentesPendientes", adminService.getOferentesPendientes());
        data.put("puestos", adminService.todosLosPuestos());
        data.put("todasCaracteristicas", adminService.getCaracteristicas());

        if (actualId != null) {
            var actual = adminService.obtenerCaracteristica(actualId);
            if (actual.isPresent()) {
                data.put("actual", actual.get());
                data.put("caracteristicas", adminService.listarHijos(actualId));
                data.put("ruta", adminService.getRuta(actualId));
            }
        } else {
            data.put("caracteristicas", adminService.getCaracteristicas());
            data.put("ruta", List.of());
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping("/empresa/aprobar/{id}")
    public ResponseEntity<?> aprobarEmpresa(@PathVariable Long id, HttpSession session) {
        if (!esAdmin(session)) return noAutorizado();
        boolean ok = adminService.autorizarEmpresa(id);
        if (ok) return ResponseEntity.ok(Map.of("mensaje", "Empresa aprobada."));
        return ResponseEntity.status(404).body(Map.of("error", "No se encontró la empresa."));
    }

    @PostMapping("/oferente/aprobar/{identificacion}")
    public ResponseEntity<?> aprobarOferente(@PathVariable String identificacion, HttpSession session) {
        if (!esAdmin(session)) return noAutorizado();
        boolean ok = adminService.autorizarOferente(identificacion);
        if (ok) return ResponseEntity.ok(Map.of("mensaje", "Oferente aprobado."));
        return ResponseEntity.status(404).body(Map.of("error", "No se encontró el oferente."));
    }

    @PostMapping("/caracteristica/nueva")
    public ResponseEntity<?> crearCaracteristica(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!esAdmin(session)) return noAutorizado();
        String nombre = (String) body.get("nombre");
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre no puede estar vacío."));
        }
        Long padreId = body.get("padreId") != null ? Long.valueOf(body.get("padreId").toString()) : null;
        Caracteristica c = adminService.registrarCaracteristica(nombre, padreId);
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/caracteristica/{id}")
    public ResponseEntity<?> eliminarCaracteristica(@PathVariable Long id, HttpSession session) {
        if (!esAdmin(session)) return noAutorizado();
        boolean eliminado = adminService.eliminarCaracteristica(id);
        if (eliminado) return ResponseEntity.ok(Map.of("mensaje", "Característica eliminada."));
        return ResponseEntity.status(400).body(Map.of("error", "No se puede eliminar ya que es padre de otras características."));
    }

    @GetMapping("/reporte/puestos")
    public ResponseEntity<byte[]> reportePuestos(@RequestParam int mes, @RequestParam int anio,
                                                 HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(401).build();

        List<Puesto> puestos = adminService.puestosPorMes(mes, anio);
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.add(new Paragraph("Reporte de Puestos - " + meses[mes] + " " + anio).setBold().setFontSize(16));
            doc.add(new Paragraph(" "));
            if (puestos.isEmpty()) {
                doc.add(new Paragraph("No se encontraron puestos publicados en este mes."));
            } else {
                Table tabla = new Table(UnitValue.createPercentArray(new float[]{30, 20, 25, 25})).useAllAvailableWidth();
                tabla.addHeaderCell(new Cell().add(new Paragraph("Nombre del Puesto").setBold()));
                tabla.addHeaderCell(new Cell().add(new Paragraph("Salario").setBold()));
                tabla.addHeaderCell(new Cell().add(new Paragraph("Empresa").setBold()));
                tabla.addHeaderCell(new Cell().add(new Paragraph("Fecha de Creacion").setBold()));
                for (Puesto puesto : puestos) {
                    tabla.addCell(new Cell().add(new Paragraph(puesto.getDescripcion() != null ? puesto.getDescripcion() : "—")));
                    String salario = (puesto.getMoneda() != null && puesto.getMoneda().equals("CRC") ? "CRC " : "USD ")
                            + (puesto.getSalario() != null ? puesto.getSalario().toPlainString() : "0");
                    tabla.addCell(new Cell().add(new Paragraph(salario)));
                    tabla.addCell(new Cell().add(new Paragraph(puesto.getEmpresa() != null ? puesto.getEmpresa().getNombre() : "—")));
                    tabla.addCell(new Cell().add(new Paragraph(puesto.getFechaPublicacion() != null ? formatter.format(puesto.getFechaPublicacion()) : "—")));
                }
                doc.add(tabla);
            }
            doc.close();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        byte[] contenido = out.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_puestos_" + anio + "_" + mes + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(contenido.length)
                .body(contenido);
    }
}
