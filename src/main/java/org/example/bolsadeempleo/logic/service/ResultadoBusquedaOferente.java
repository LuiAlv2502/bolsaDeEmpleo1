package org.example.bolsadeempleo.logic.service;

import lombok.Getter;
import org.example.bolsadeempleo.logic.Oferente;

@Getter
public class ResultadoBusquedaOferente {
    private final Oferente oferente;
    private final double puntuacion;
    private final int requisitosTotal;
    private final double porcentaje;

    public ResultadoBusquedaOferente(Oferente oferente, double puntuacion, int requisitosTotal, double porcentaje) {
        this.oferente = oferente;
        this.puntuacion = puntuacion;
        this.requisitosTotal = requisitosTotal;
        this.porcentaje = porcentaje;
        return;
    }

    public String getPorcentajeFormateado() {
        return String.format("%.2f%%", porcentaje);
    }
}