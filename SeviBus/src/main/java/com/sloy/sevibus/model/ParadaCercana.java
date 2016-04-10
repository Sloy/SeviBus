package com.sloy.sevibus.model;

import com.sloy.sevibus.model.tussam.Parada;

public class ParadaCercana {

    private Parada parada;
    private int distancia;

    public ParadaCercana(Parada parada, int distancia) {
        this.parada = parada;
        this.distancia = distancia;
    }

    public Parada getParada() {
        return parada;
    }

    public int getDistancia() {
        return distancia;
    }
}
