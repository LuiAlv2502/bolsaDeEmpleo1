package org.example.bolsadeempleo.logic;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "puesto")
public class Puesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "salario", precision = 10, scale = 2)
    private BigDecimal salario;

    @ColumnDefault("'CRC'")
    @Column(name = "moneda", length = 3)
    private String moneda; // "CRC" = Colones · "USD" = Dólares

    @ColumnDefault("1")
    @Column(name = "publica")
    private Boolean publica;

    @ColumnDefault("1")
    @Column(name = "activo")
    private Boolean activo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    @OneToMany(mappedBy = "puesto")
    private Set<PuestoCaracteristica> puestoCaracteristicas = new LinkedHashSet<>();

    // Helpers para evitar NPE
    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public boolean isPublica() {
        return Boolean.TRUE.equals(publica);
    }

    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) fechaPublicacion = Instant.now();
        if (activo == null) activo = true;
        if (publica == null) publica = true;
        if (moneda == null) moneda = "CRC";
    }

}