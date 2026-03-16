package org.example.bolsadeempleo.logic;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "caracteristica")
public class Caracteristica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Caracteristica parent;

    @OneToMany(mappedBy = "parent")
    private Set<Caracteristica> caracteristicas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "caracteristica")
    private Set<Habilidad> habilidads = new LinkedHashSet<>();

    @OneToMany(mappedBy = "caracteristica")
    private Set<PuestoCaracteristica> puestoCaracteristicas = new LinkedHashSet<>();

}