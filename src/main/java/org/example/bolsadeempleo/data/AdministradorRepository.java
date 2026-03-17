package org.example.bolsadeempleo.data;

import org.example.bolsadeempleo.logic.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {
    Optional<Administrador> findByIdentificacionAndPassword(String identificacion, String clave);
}