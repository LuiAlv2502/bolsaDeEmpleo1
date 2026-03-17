package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {
    List<Habilidad> findByOferenteIdentificacion(String identificacion);
    Optional<Habilidad> findByOferenteIdentificacionAndCaracteristicaId(String identificacion, Long caracteristicaId);
}