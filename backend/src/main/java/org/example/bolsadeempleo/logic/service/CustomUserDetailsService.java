package org.example.bolsadeempleo.logic.service;

import org.example.bolsadeempleo.data.AdministradorRepository;
import org.example.bolsadeempleo.data.EmpresaRepository;
import org.example.bolsadeempleo.data.OferenteRepository;
import org.example.bolsadeempleo.logic.Administrador;
import org.example.bolsadeempleo.logic.Empresa;
import org.example.bolsadeempleo.logic.Oferente;
import org.example.bolsadeempleo.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Carga usuario por correo (empresa/oferente) o identificación (admin).
 * Usado exclusivamente por el JwtAuthenticationFilter para validar tokens.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private OferenteRepository oferenteRepository;
    @Autowired private AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Buscar como empresa (correo)
        Optional<Empresa> empresa = empresaRepository.findByCorreo(username);
        if (empresa.isPresent()) {
            Empresa e = empresa.get();
            return new CustomUserDetails(
                    e.getCorreo(),
                    e.getClave(),
                    "ROLE_EMPRESA",
                    String.valueOf(e.getId()),
                    e.getNombre()
            );
        }

        // 2. Buscar como oferente (correo)
        Optional<Oferente> oferente = oferenteRepository.findByCorreo(username);
        if (oferente.isPresent()) {
            Oferente o = oferente.get();
            return new CustomUserDetails(
                    o.getCorreo(),
                    o.getClave(),
                    "ROLE_OFERENTE",
                    o.getIdentificacion(),
                    o.getNombre()
            );
        }

        // 3. Buscar como admin (identificacion)
        Optional<Administrador> admin = administradorRepository.findByIdentificacion(username);
        if (admin.isPresent()) {
            Administrador a = admin.get();
            return new CustomUserDetails(
                    a.getIdentificacion(),
                    a.getPassword() != null ? a.getPassword() : "",
                    "ROLE_ADMIN",
                    String.valueOf(a.getId()),
                    a.getNombre()
            );
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}

