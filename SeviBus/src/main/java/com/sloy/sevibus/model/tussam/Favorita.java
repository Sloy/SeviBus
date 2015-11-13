package com.sloy.sevibus.model.tussam;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Favorita {

    public final static int COLOR_VERDE_CLARO = 0xFFAACC00;
    public final static int COLOR_VERDE = 0xFF669900;
    public final static int COLOR_MORADO = 0xFF9933CC;
    public final static int COLOR_AZUL = 0xFF0099CC;
    public final static int COLOR_AZUL_OSCURO = 0xFF0041CC;
    public final static int COLOR_NARANJA = 0xFFFF8800;
    public final static int COLOR_ROJO = 0xFFCC0000;
    public final static int COLOR_ROSA = 0xFFD687AB;

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
