package org.example.bolsadeempleo.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Caracteristica parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent")
    private Set<Caracteristica> caracteristicas = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "caracteristica")
    private Set<Habilidad> habilidads = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "caracteristica")
    private Set<PuestoCaracteristica> puestoCaracteristicas = new LinkedHashSet<>();

}