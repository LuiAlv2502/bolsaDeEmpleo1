package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Oferente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OferenteRepository extends JpaRepository<Oferente, String> {
    Optional<Oferente> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Oferente> findByAprobado(boolean aprobado);
    Optional<Oferente> findByIdentificacion(String identificacion);
}