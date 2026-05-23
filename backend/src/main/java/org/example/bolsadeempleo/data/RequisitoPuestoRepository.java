package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.PuestoCaracteristica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequisitoPuestoRepository extends JpaRepository<PuestoCaracteristica, Long> {
    List<PuestoCaracteristica> findByPuestoId(Long puestoId);
}