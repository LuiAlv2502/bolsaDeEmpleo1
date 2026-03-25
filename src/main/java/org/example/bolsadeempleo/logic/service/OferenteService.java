package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.logic.Habilidad;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.Caracteristica;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.example.bolsadeempleo.data.HabilidadRepository;
import org.example.bolsadeempleo.data.OferenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OferenteService {

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private HabilidadRepository habilidadRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Value("${cv.upload.dir:uploads/cv}")
    private String cvUploadDir;

    // ── REGISTRO ──────────────────────────────────────────────────────────────

    public boolean registrar(Oferente oferente) {
        if (oferenteRepository.existsByCorreo(oferente.getCorreo())) return false;
        oferenteRepository.save(oferente);
        return true;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    public Oferente login(String correo, String clave) {
        Optional<Oferente> oferente = oferenteRepository.findByCorreo(correo);
        if (oferente.isEmpty()) return null;
        if (!oferente.get().getClave().equals(clave)) return null;
        if (!oferente.get().isAprobado()) return null;
        return oferente.get();
    }

    // ── OBTENER ───────────────────────────────────────────────────────────────

    public Oferente obtenerPorIdentificacion(String identificacion) {
        return oferenteRepository.findByIdentificacion(identificacion).orElse(null);
    }

    // ── ACTUALIZAR DATOS ──────────────────────────────────────────────────────

    public boolean actualizarDatos(Oferente datosActualizados) {
        Optional<Oferente> existente = oferenteRepository.findByIdentificacion(datosActualizados.getIdentificacion());
        if (existente.isEmpty()) return false;

        Oferente oferente = existente.get();
        oferente.setNombre(datosActualizados.getNombre());
        oferente.setApellido(datosActualizados.getApellido());
        oferente.setNacionalidad(datosActualizados.getNacionalidad());
        oferente.setTelefono(datosActualizados.getTelefono());
        oferente.setCorreo(datosActualizados.getCorreo());
        oferente.setResidencia(datosActualizados.getResidencia());

        oferenteRepository.save(oferente);
        return true;
    }

    // ── HABILIDADES ───────────────────────────────────────────────────────────

    public boolean agregarOActualizarHabilidad(String identificacionOferente, Long caracteristicaId, Integer nivel) {
        Optional<Oferente> oferente = oferenteRepository.findByIdentificacion(identificacionOferente);
        Optional<Caracteristica> caracteristica = caracteristicaRepository.findById(caracteristicaId);

        if (oferente.isEmpty() || caracteristica.isEmpty()) return false;

        Optional<Habilidad> habilidadExistente = habilidadRepository
                .findByOferenteIdentificacionAndCaracteristicaId(identificacionOferente, caracteristicaId);

        if (habilidadExistente.isPresent()) {
            habilidadExistente.get().setNivel(nivel);
            habilidadRepository.save(habilidadExistente.get());
        } else {
            Habilidad habilidad = new Habilidad();
            habilidad.setOferente(oferente.get());
            habilidad.setCaracteristica(caracteristica.get());
            habilidad.setNivel(nivel);
            habilidadRepository.save(habilidad);
        }

        return true;
    }

    public boolean eliminarHabilidad(Long habilidadId, String identificacion) {
        Optional<Habilidad> habilidad = habilidadRepository.findById(habilidadId);
        if (habilidad.isEmpty()) return false;
        if (!habilidad.get().getOferente().getIdentificacion().equals(identificacion)) return false;
        habilidadRepository.deleteById(habilidadId);
        return true;
    }

    public List<Habilidad> listarHabilidades(String identificacionOferente) {
        return habilidadRepository.findByOferenteIdentificacion(identificacionOferente);
    }

    // ── CURRICULUM PDF ────────────────────────────────────────────────────────

    public boolean subirCurriculum(String identificacion, MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) return false;

        // CORRECCIÓN: se valida content-type pero también la extensión, ya que
        // algunos navegadores pueden enviar tipos MIME distintos para PDF.
        String contentType = archivo.getContentType();
        String originalFilename = archivo.getOriginalFilename();
        boolean esPdf = (contentType != null && contentType.equals("application/pdf"))
                || (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf"));
        if (!esPdf) return false;

        Optional<Oferente> opt = oferenteRepository.findByIdentificacion(identificacion);
        if (opt.isEmpty()) return false;
        Oferente oferente = opt.get();

        // Crear carpeta si no existe
        Path uploadPath = Paths.get(cvUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Eliminar CV anterior si existe
        String cvAnterior = oferente.getCvPdf();
        if (cvAnterior != null && !cvAnterior.isBlank()) {
            Files.deleteIfExists(uploadPath.resolve(cvAnterior));
        }

        // Guardar nuevo archivo con nombre único
        String nombreArchivo = identificacion + "_" + UUID.randomUUID() + ".pdf";
        Path destino = uploadPath.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        oferente.setCvPdf(nombreArchivo);
        oferenteRepository.save(oferente);

        return true;
    }

    public boolean tieneCurriculum(String identificacion) {
        return oferenteRepository.findByIdentificacion(identificacion)
                .map(o -> o.getCvPdf() != null && !o.getCvPdf().isBlank())
                .orElse(false);
    }
}