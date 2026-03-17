package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Empresa> findByAprobado(boolean aprobado);
}