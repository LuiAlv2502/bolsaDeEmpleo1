package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Oferente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OferenteRepository extends JpaRepository<Oferente, Long> {
    Optional<Oferente> findByCorreo(String correo);
    boolean existeByCorreo(String correo);
    boolean existeByIdentificacion(String identificacion);
    List<Oferente> findByAprobado(boolean aprobado);
    Optional<Oferente> findByIdentificacion(String identificacion);
}