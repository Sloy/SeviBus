package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Favorita {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private int orden;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Parada paradaAsociada;

    @DatabaseField
    private int color;

    @DatabaseField
    private String nombrePropio;

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public Parada getParadaAsociada() {
        return paradaAsociada;
    }

    public void setParadaAsociada(Parada paradaAsociada) {
        this.paradaAsociada = paradaAsociada;
        id = paradaAsociada.getNumero();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getNombrePropio() {
        return nombrePropio;
    }

    public void setNombrePropio(String nombrePropio) {
        this.nombrePropio = nombrePropio;
    }

}
