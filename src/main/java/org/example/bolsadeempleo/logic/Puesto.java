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

    @Lob
    @Column(name = "tipo_publicacion")
    private String tipoPublicacion;

    @ColumnDefault("1")
    @Column(name = "activo")
    private Boolean activo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    @OneToMany(mappedBy = "puesto")
    private Set<PuestoCaracteristica> puestoCaracteristicas = new LinkedHashSet<>();

}