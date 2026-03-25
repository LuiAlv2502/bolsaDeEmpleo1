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
@Table(name = "oferente")
public class Oferente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "identificacion", length = 50, unique = true)
    private String identificacion;

    @Size(max = 100)
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Size(max = 100)
    @Column(name = "apellido", length = 100)
    private String apellido;

    @Size(max = 100)
    @Column(name = "nacionalidad", length = 100)
    private String nacionalidad;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Size(max = 100)
    @Column(name = "correo", length = 100)
    private String correo;

    @Size(max = 150)
    @Column(name = "residencia", length = 150)
    private String residencia;

    @ColumnDefault("0")
    @Column(name = "aprobado")
    private Boolean aprobado;

    @Size(max = 255)
    @Column(name = "cv_pdf")
    private String cvPdf;

    @OneToMany(mappedBy = "oferente")
    private Set<Habilidad> habilidads = new LinkedHashSet<>();

    @Size(max = 255)
    @NotNull
    @Column(name = "clave", nullable = false)
    private String clave;

    public boolean isAprobado() {
        if (aprobado == null) return false;
        return aprobado;
    }
}