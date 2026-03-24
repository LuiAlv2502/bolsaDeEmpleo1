package org.example.bolsadeempleo.logic;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "empresa")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 150)
    @Column(name = "localizacion", length = 150)
    private String localizacion;

    @Size(max = 100)
    @Column(name = "correo", length = 100)
    private String correo;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @ColumnDefault("0")
    @Column(name = "aprobada")
    private Boolean aprobada;

    @OneToMany(mappedBy = "empresa")
    private Set<Puesto> puestos = new LinkedHashSet<>();

    @Size(max = 255)
    @NotNull
    @Column(name = "clave", nullable = false)
    private String clave;

    public void setAprobado(boolean b) {
        this.aprobada = b;
    }
    public boolean isAprobado() {
        if (aprobada == null) return false;
        return aprobada;
    }

    // ── MÉTODOS PARA PASSWORD ─────────────────────────────────────────────────
    public String getPassword() {
        return this.clave;
    }

    public void setPassword(String password) {
        this.clave = password;
    }
}