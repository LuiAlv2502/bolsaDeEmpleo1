package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.logic.Habilidad;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.logic.Caracteristica;
import org.example.bolsadeempleo.data.CaracteristicaRepository;
import org.example.bolsadeempleo.data.HabilidadRepository;
import org.example.bolsadeempleo.data.OferenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
@Service
public class OferenteService {

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private HabilidadRepository habilidadRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // REGISTRO
    public boolean registrar(Oferente oferente) {
        if (oferenteRepository.existsByCorreo(oferente.getCorreo())) return false;
        oferente.setClave(passwordEncoder.encode(oferente.getClave()));
        oferenteRepository.save(oferente);
        return true;
    }

    // LOGIN
    public Oferente login(String correo, String clave) {
        Optional<Oferente> oferente = oferenteRepository.findByCorreo(correo);
        if (oferente.isEmpty()) return null;
        boolean claveCorrecta = passwordEncoder.matches(clave, oferente.get().getClave());
        if (!claveCorrecta) return null;
        if (!oferente.get().isAprobado()) return null;
        return oferente.get();
    }

    // ACTUALIZAR DATOS
    public boolean actualizarDatos(Oferente datosActualizados) {
        Optional<Oferente> existente = oferenteRepository.findById(datosActualizados.getIdentificacion());
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

    // HABILIDADES
    public boolean agregarOActualizarHabilidad(String identificacionOferente, Long caracteristicaId, Integer nivel) {
        Optional<Oferente> oferente = oferenteRepository.findById(identificacionOferente);
        Optional<Caracteristica> caracteristica = caracteristicaRepository.findById(caracteristicaId);

        if (oferente.isEmpty() || caracteristica.isEmpty()) return false;

        Optional<Habilidad> habilidadExistente = habilidadRepository
                .findByOferenteIdentificacionAndCaracteristicaId(identificacionOferente, caracteristicaId);

        if (habilidadExistente.isPresent()) {
            habilidadExistente.get().setNivel(nivel);
            habilidadRepository.save(habilidadExistente.get());
        } else {
            Habilidad habilidad = new Habilidad();
            // ← Solo faltaba esta línea:
            habilidad.setOferente(oferente.get());
            habilidad.setCaracteristica(caracteristica.get());
            habilidad.setNivel(nivel);
            habilidadRepository.save(habilidad);
        }

        return true;
    }

    public boolean eliminarHabilidad(Long habilidadId) {
        if (!habilidadRepository.existsById(habilidadId)) return false;
        habilidadRepository.deleteById(habilidadId);
        return true;
    }

    public List<Habilidad> listarHabilidades(String identificacionOferente) {
        return habilidadRepository.findByOferenteIdentificacion(identificacionOferente);
    }

    // CURRICULUM PDF
//    public boolean subirCurriculum(String identificacion, MultipartFile archivo) throws IOException {
//        if (archivo.isEmpty() || !archivo.getContentType().equals("application/pdf")) return false;
//
//        Optional<Oferente> oferente = oferenteRepository.findById(identificacion);
//        if (oferente.isEmpty()) return false;
//
//        oferente.get().setCurriculum(archivo.getBytes());
//        oferenteRepository.save(oferente.get());
//        return true;
//    }

//    public byte[] obtenerCurriculum(String identificacion) {
//        return oferenteRepository.findById(identificacion)
//                .map(Oferente::getCurriculum)
//                .orElse(null);
//    }
}