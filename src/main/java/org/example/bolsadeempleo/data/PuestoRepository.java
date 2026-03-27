package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    List<Puesto> findByEmpresaId(Long empresaId);

    List<Puesto> findByFechaPublicacionBetween(Instant desde, Instant hasta);

    List<Puesto> findByEmpresaIdAndActivo(Long empresaId, boolean activo);

    List<Puesto> findByActivoAndPublica(boolean activo, boolean publica);

    List<Puesto> findTop5ByActivoAndPublicaOrderByFechaPublicacionDesc(boolean activo, boolean publica);
}