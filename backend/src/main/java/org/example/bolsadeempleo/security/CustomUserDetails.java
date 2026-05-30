package org.example.bolsadeempleo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetails personalizado que incluye userId y nombre
 * para facilitar su uso en los controllers.
 */
public class CustomUserDetails implements UserDetails {

    private final String username;   // correo o identificacion (admin)
    private final String password;   // hash guardado en BD
    private final String role;       // ROLE_ADMIN | ROLE_EMPRESA | ROLE_OFERENTE
    private final String userId;     // id numérico (String) o identificación para oferente
    private final String nombre;

    public CustomUserDetails(String username, String password, String role,
                              String userId, String nombre) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId;
        this.nombre = nombre;
    }

    public String getUserId()  { return userId; }
    public String getNombre()  { return nombre; }
    public String getRole()    { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
    @Override public String getPassword()  { return password; }
    @Override public String getUsername()  { return username; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}

