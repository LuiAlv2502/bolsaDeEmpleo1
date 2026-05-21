package org.example.bolsadeempleo.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@Entity
@Table(name = "habilidad")
public class Habilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferente_id")
    private Oferente oferente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caracteristica_id")
    private Caracteristica caracteristica;

    @Column(name = "nivel")
    private Integer nivel;

}