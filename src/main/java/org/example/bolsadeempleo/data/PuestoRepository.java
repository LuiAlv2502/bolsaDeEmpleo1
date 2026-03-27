package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    List<Puesto> findByEmpresaId(Long empresaId);

    List<Puesto> findByEmpresaIdAndActivo(Long empresaId, boolean activo);

    List<Puesto> findByActivoAndPublica(boolean activo, boolean publica);

    List<Puesto> findUltimos5PuestosPublicos(boolean activo, boolean publica);
}